package hua.ocr.read.ui

import android.graphics.Point
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

fun mapToCanvas(
    points: List<Point>,
    imageWidth: Int,
    imageHeight: Int,
    canvasSize: IntSize
): List<Offset> {

    if (imageWidth == 0 || imageHeight == 0) return emptyList()

    val scale = min(
        canvasSize.width.toFloat() / imageWidth,
        canvasSize.height.toFloat() / imageHeight
    )

    val dx = (canvasSize.width - imageWidth * scale) / 2f
    val dy = (canvasSize.height - imageHeight * scale) / 2f

    return points.map {
        Offset(
            x = it.x * scale + dx,
            y = it.y * scale + dy
        )
    }
}

fun isPointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    var inside = false
    var j = polygon.size - 1

    for (i in polygon.indices) {
        val xi = polygon[i].x
        val yi = polygon[i].y
        val xj = polygon[j].x
        val yj = polygon[j].y

        val intersect = ((yi > point.y) != (yj > point.y)) &&
                (point.x < (xj - xi) * (point.y - yi) / (yj - yi + 0.00001f) + xi)

        if (intersect) inside = !inside
        j = i
    }

    return inside
}