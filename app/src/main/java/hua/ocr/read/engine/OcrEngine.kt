package hua.ocr.read.engine

import android.graphics.Bitmap
import android.graphics.Rect
import hua.ocr.read.bean.OcrBlock

interface OcrEngine {

    /** 初始化（可重复调用，但只会执行一次） */
    suspend fun init()

    /** 是否已初始化 */
    val isInitialized: Boolean

    /** 识别 */
    suspend fun recognize(bitmap: Bitmap): Result<List<OcrBlock>>

    /** 释放资源 */
    fun release()

    fun mergeBlocks(blocks: List<OcrBlock>): List<OcrBlock> {

        if (blocks.isEmpty()) return emptyList()

        val sorted = blocks.sortedBy { it.rect.top }

        val groups = mutableListOf<MutableList<OcrBlock>>()

        for (block in sorted) {

            val lastGroup = groups.lastOrNull()

            if (lastGroup == null) {
                groups.add(mutableListOf(block))
                continue
            }

            val last = lastGroup.last()

            val verticalGap = block.rect.top - last.rect.bottom
            val leftDiff = kotlin.math.abs(block.rect.left - last.rect.left)

            val isSameLine = verticalGap < 40 && leftDiff < 40

            if (isSameLine) {
                lastGroup.add(block)
            } else {
                groups.add(mutableListOf(block))
            }
        }

        return groups.map { group ->
            val text = group.joinToString("\n") { it.text }

            val left = group.minOf { it.rect.left }
            val top = group.minOf { it.rect.top }
            val right = group.maxOf { it.rect.right }
            val bottom = group.maxOf { it.rect.bottom }

            OcrBlock(
                text = text,
                rect = Rect(left, top, right, bottom)
            )
        }
    }
}