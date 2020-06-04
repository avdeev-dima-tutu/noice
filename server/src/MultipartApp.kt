package io.ktor.samples.clientmultipart

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.*

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        routing {
            post("/upload") {
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            "FormItem(${part.name},${part.value})"
                        }
                        is PartData.FileItem -> {
                            val bytes = part.streamProvider().readBytes()
                            "FileItem(${part.name},${part.originalFileName},${hex(bytes)})"
                            val file = File(part.originalFileName)

                            if (!file.canonicalPath.contains(File(".").canonicalPath)) {
                                //Еслм пытаемся получить доступ к директории вне проекта, то кидаем ошибку
                                throw Error("not secure access to file: $file")
                            }
                            if (file.exists()) {
                                file.delete()
                            }
                            file.parentFile?.mkdirs()
                            file.createNewFile()
                            file.writeBytes(bytes)
                        }
                        is PartData.BinaryItem -> {
                            "BinaryItem(${part.name},${hex(part.provider().readBytes())})"
                        }
                    }
                    part.dispose()
                }
                call.respondText("done")
            }
        }
    }.start(wait = false)
    delay(1000)
    sendMultipart()
}

suspend fun sendMultipart(): String {
    val client = HttpClient(Apache)
    val result = client.post<HttpResponse>("http://127.0.0.1:8080/upload") {
        body = MultiPartContent.build {
            add("user", "myuser")
            add("password", "password")
            add("file", byteArrayOf(1, 2, 3, 4), filename = "file.bin")
        }
    }
    return result.content.readRemaining().readText()
}

class MultiPartContent(val parts: List<Part>) : OutgoingContent.WriteChannelContent() {
    val uuid = UUID.randomUUID()
    val boundary = "***ktor-$uuid-ktor-${System.currentTimeMillis()}***"

    data class Part(
        val name: String,
        val filename: String? = null,
        val headers: Headers = Headers.Empty,
        val writer: suspend ByteWriteChannel.() -> Unit
    )

    override suspend fun writeTo(channel: ByteWriteChannel) {
        for (part in parts) {
            channel.writeStringUtf8("--$boundary\r\n")
            val partHeaders = Headers.build {
                val fileNamePart = if (part.filename != null) "; filename=\"${part.filename}\"" else ""
                append("Content-Disposition", "form-data; name=\"${part.name}\"$fileNamePart")
                appendAll(part.headers)
            }
            for ((key, value) in partHeaders.flattenEntries()) {
                channel.writeStringUtf8("$key: $value\r\n")
            }
            channel.writeStringUtf8("\r\n")
            part.writer(channel)
            channel.writeStringUtf8("\r\n")
        }
        channel.writeStringUtf8("--$boundary--\r\n")
    }

    override val contentType = ContentType.MultiPart.FormData
        .withParameter("boundary", boundary)
        .withCharset(Charsets.UTF_8)

    class Builder {
        val parts = arrayListOf<Part>()

        fun add(part: Part) {
            parts += part
        }

        fun add(
            name: String,
            filename: String? = null,
            contentType: ContentType? = null,
            headers: Headers = Headers.Empty,
            writer: suspend ByteWriteChannel.() -> Unit
        ) {
            val contentTypeHeaders: Headers =
                if (contentType != null) headersOf(HttpHeaders.ContentType, contentType.toString()) else headersOf()
            add(Part(name, filename, headers + contentTypeHeaders, writer))
        }

        fun add(name: String, text: String, contentType: ContentType? = null, filename: String? = null) {
            add(name, filename, contentType) { writeStringUtf8(text) }
        }

        fun add(
            name: String,
            data: ByteArray,
            contentType: ContentType? = ContentType.Application.OctetStream,
            filename: String? = null
        ) {
            add(name, filename, contentType) { writeFully(data) }
        }

        internal fun build(): MultiPartContent = MultiPartContent(parts.toList())
    }

    companion object {
        fun build(callback: Builder.() -> Unit) = Builder().apply(callback).build()
    }
}

operator fun Headers.plus(other: Headers): Headers = when {
    this.isEmpty() -> other
    other.isEmpty() -> this
    else -> Headers.build {
        appendAll(this@plus)
        appendAll(other)
    }
}
