package hua.ocr.read.engine.paddle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.equationl.ncnnandroidppocr.OCR
import com.equationl.ncnnandroidppocr.bean.Device
import com.equationl.ncnnandroidppocr.bean.DrawModel
import com.equationl.ncnnandroidppocr.bean.ImageSize
import com.equationl.ncnnandroidppocr.bean.ModelType
import com.equationl.ncnnandroidppocr.bean.OcrTextLineResult
import hua.ocr.read.bean.OcrBlock
import hua.ocr.read.engine.BaseOcrEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PaddleOcrEngine(
    private val context: Context
) : BaseOcrEngine() {

    private val ocr = OCR()

    override suspend fun onInit(): Boolean {
        val result = ocr.initModelFromAssert(
            context.assets,
            ModelType.Mobile,
            ImageSize.Size720,
            Device.CPU
        )
        return result
    }

    override suspend fun recognize(bitmap: Bitmap): Result<List<OcrBlock>> {
        if (!isInitialized) {
            return Result.failure(RuntimeException("ocr engine not initialized"))
        }
        return suspendCancellableCoroutine { count ->
            val result = ocr.detectBitmap(bitmap, drawModel = DrawModel.None)
            if (result == null) {
                count.resume(Result.failure(RuntimeException("ocr engine can not recognize bitmap")))
            }
            else {
                val blocks = result.textLines.mapNotNull {
                    it.toBlock()
                }
                count.resume(Result.success(mergeBlocks(blocks)))
            }
        }
    }

    private fun OcrTextLineResult.toBlock(): OcrBlock? {
        if (points.isEmpty()) return null

        val xs = points.map { it.x }
        val ys = points.map { it.y }

        val rect = Rect(
            xs.minOrNull() ?: return null,
            ys.minOrNull() ?: return null,
            xs.maxOrNull() ?: return null,
            ys.maxOrNull() ?: return null
        )

        return OcrBlock(
            text = text,
            rect = rect,
            confidence = confidence
        )
    }

    override fun release() {
        ocr.release()
    }
}