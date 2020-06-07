package com.test.ui_test_core.snapshot

import android.os.Environment
import com.test.ui_test_core.utils.toBitmap
import com.test.ui_test_core.utils.toBytesAndRecycle

class Snapshot @PublishedApi internal constructor(val byteArray: ByteArray) {
    override fun equals(other: Any?) = (other as? Snapshot)?.byteArray?.contentEquals(byteArray) ?: false
    override fun hashCode(): Int = byteArray.contentHashCode()
}

class ExpectSnapshotData @PublishedApi internal constructor(val snapshot: Snapshot, val pngName: String)

/**
 * Загружаем подготовленный скриншот из директории ./resources/
 */
@PublishedApi
internal inline fun <reified T> T.getSnapshotFromResources(pngName: String): ByteArray {
    // Байты в неизвестной кодировке изображения (PNG-24, PNG-32, WebP). Зависит от того как мы сохранили картинку.
    // Главное не использовать JPEG или форматы с потерей качества.
    val unknownEncodeBytes = T::class.java.classLoader?.getResource(pngName)?.readBytes()

    // Преобразуем в Bitmap и обратно в ByteArray чтобы получить кодировку PNG по-умолчанию для Android
    val bytes = unknownEncodeBytes?.toBitmap()?.toBytesAndRecycle()

    // На выходе получаем байты в кодировке PNG по умолчанию для Android
    return bytes ?: byteArrayOf()
}

internal fun Snapshot.saveOnAndroidStorage(fileName: String) {
    val file = Environment.getExternalStorageDirectory().resolve(fileName)
    file.parentFile.mkdirs()
    file.createNewFile()
    file.writeBytes(byteArray)
}
