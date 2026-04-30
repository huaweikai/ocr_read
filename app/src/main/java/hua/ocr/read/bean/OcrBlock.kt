package hua.ocr.read.bean

import android.graphics.Point

data class OcrBlock(
    val text: String,
    val cornerPoints: List<Point>
)