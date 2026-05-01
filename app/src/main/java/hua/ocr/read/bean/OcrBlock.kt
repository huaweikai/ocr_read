package hua.ocr.read.bean

import android.graphics.Rect

data class OcrBlock(
    val text: String,
    val rect: Rect, // 👈 直接给 UI 用
    val confidence: Float = 1f
)