package com.test.ui_test_core.snapshot

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.runBlocking
import ru.tutu.snapshot.upload.sendMultipart

fun assertSnapshot2(gradleModuleDir: String, expect: ExpectSnapshotData, actual: Snapshot, description: String = "") {
    runBlocking {
        HttpClient(Android).use { httpClient ->
            httpClient.sendMultipart(
                uploadUrl = "http://10.0.2.2:8080/upload",
                filePath = "$gradleModuleDir/src/androidTest/resources/${expect.pngName}",
                fileBytes = actual.byteArray
            )
        }
    }
    assertSnapshot(expect, actual, description)
}