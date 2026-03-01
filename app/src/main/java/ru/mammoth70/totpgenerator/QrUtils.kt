package ru.mammoth70.totpgenerator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

@Throws(WriterException::class)
fun generateQrCode(content: String, sizePx: Int = 1024): Bitmap {
    // Функция генерирует QR-код из строки.

    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx
    )

    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap[x, y] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
        }
    }
    return bitmap
}