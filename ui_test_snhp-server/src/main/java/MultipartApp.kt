package io.ktor.samples.clientmultipart

import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.hex
import io.ktor.utils.io.core.readBytes
import java.io.File

suspend fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080,configure = {
        //конфигурация может быть специфичная для Netty или CIO
    connectionGroupSize
    workerGroupSize
    callGroupSize
    }) {
        routing {
            get("/") {
                call.respondText { "save snapshot server" }
            }
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
}
