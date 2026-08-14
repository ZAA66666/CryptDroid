package com.zaa.cryptdroid.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * QrUtil — 二维码生成（ZXing core，Apache 2.0）
 */
object QrUtil {

    /**
     * 生成二维码 Bitmap
     * @param content 内容
     * @param size 边长（像素）
     * @param foreground 前景色（如 0xFF000000）
     * @param background 背景色（如 0xFFFFFFFF）
     */
    fun generateQr(
        content: String,
        size: Int = 512,
        foreground: Int = Color.BLACK,
        background: Int = Color.WHITE,
        errorLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val hints = mapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to errorLevel,
                EncodeHintType.MARGIN to 1
            )
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val pixels = IntArray(size * size)
            for (y in 0 until size) {
                for (x in 0 until size) {
                    pixels[y * size + x] = if (matrix.get(x, y)) foreground else background
                }
            }
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, size, 0, 0, size, size)
            }
        } catch (e: Exception) {
            null
        }
    }
}
