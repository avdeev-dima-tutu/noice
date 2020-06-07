package ru.tutu.snapshot.upload

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.util.flattenEntries
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writeStringUtf8
import java.util.*

suspend fun HttpClient.sendMultipart(
    uploadUrl: String,
    filePath: String,
    fileBytes: ByteArray
): String {
    val response = post<HttpResponse>("$uploadUrl/upload") {
        body = MultiPartContent.build {
            add("file", fileBytes, filename = filePath)
        }
    }
    return response.content.readRemaining().readText()
}

//Copy from https://github.com/ktorio/ktor-samples/blob/e929446c911037f52f6d99776edb9edc7c6b5e32/other/client-multipart/src/MultipartApp.kt
class MultiPartContent(private val parts: List<Part>) : OutgoingContent.WriteChannelContent() {
    private val uuid: UUID = UUID.randomUUID()
    private val boundary = "***ktor-$uuid-ktor-${System.currentTimeMillis()}***"

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
        private val parts = arrayListOf<Part>()

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
