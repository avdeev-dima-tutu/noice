package com.test.ui_test_core.snapshot

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.runBlocking
import ru.tutu.snapshot.upload.sendMultipart

fun assertSnapshot2(expect: ExpectSnapshotData, actual: Snapshot, description: String = "") {
    runBlocking {
        val modulePath = "app"//todo provide in gradle

        sendMultipart(
            uploadServer = "http://10.0.2.2:8082",
            name = "$modulePath/androidTest/resources/${expect.pngName}",
            fileBytes = actual.byteArray
        )
    }
    assertSnapshot(expect, actual, description)
}

suspend fun sendMultipart(uploadServer: String = "http://127.0.0.1:8082", name:String = "file.bin", fileBytes: ByteArray = byteArrayOf(1, 2, 3, 4)): String {
    return HttpClient(Android).sendMultipart(
        uploadServer = uploadServer,
        name = name,
        fileBytes = fileBytes
    )
}
