package com.test.ui_test_core.snapshot

import android.app.Activity
import android.view.View
import com.test.ui_test_core.utils.toBitmap
import com.test.ui_test_core.utils.toBytesAndRecycle
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ru.tutu.snapshot.upload.sendMultipart

class SnapshotAssertError internal constructor(pngName: String, description: String)
    : AssertionError("Expected snapshot differ ./androidTest/resources/$pngName; $description")

/**
 * Публичное API для Snapshot тестирования
 */
fun assertSnapshot(expect: ExpectSnapshotData, actual: Snapshot, description: String = "") {
    val pngName = expect.pngName.replace("/", "_").removeSuffix(".png")
    actual.saveOnAndroidStorage("screenshots/${pngName}_actual.png")
    expect.snapshot.saveOnAndroidStorage("screenshots/${pngName}_expect.png")
    if (actual != expect.snapshot) {
        throw SnapshotAssertError(pngName, description)
    }
}

inline fun <reified T> T.expectSnapshot(pngName: String): ExpectSnapshotData {
    val expectSnapshot: ByteArray = getSnapshotFromResources(pngName)
    return ExpectSnapshotData(Snapshot(expectSnapshot), pngName)
}

fun Activity.snapshot() = window.decorView.snapshot()

fun View.snapshot() =
        Snapshot(toBitmap().toBytesAndRecycle())

//todo move
suspend fun sendMultipart(uploadServer: String = "http://127.0.0.1:8080", name:String = "file.bin", fileBytes: ByteArray = byteArrayOf(1, 2, 3, 4)): String {
    return HttpClient(Android).sendMultipart(
        uploadServer = uploadServer,
        name = name,
        fileBytes = fileBytes
    )
}
