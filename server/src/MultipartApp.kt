package io.ktor.samples.clientmultipart

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import ru.tutu.snapshot.upload.sendMultipart
import java.io.File

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
    sendMultipart()//todo delete
}

