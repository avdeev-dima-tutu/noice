package ru.tutu.snapshot.upload

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.util.flattenEntries
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import java.util.*
import io.ktor.utils.io.*

suspend fun HttpClient.sendMultipart(uploadServer: String = "http://127.0.0.1:8080", name:String = "file.bin", fileBytes: ByteArray = byteArrayOf(1, 2, 3, 4)): String {
    val result =post<HttpResponse>("$uploadServer/upload") {
        val buildBody = MultiPartContent.build {
            add("user", "myuser")
            add("password", "password")
            add("file", fileBytes, filename = name)
        }
        body = buildBody
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
