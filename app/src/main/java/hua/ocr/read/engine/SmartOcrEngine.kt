package hua.ocr.read.engine

import android.graphics.Bitmap
import hua.ocr.read.bean.OcrBlock
import hua.ocr.read.engine.mk_lit.MlKitOcrEngine
import hua.ocr.read.engine.paddle.PaddleOcrEngine
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SmartOcrEngine(
    private val mlKit: MlKitOcrEngine,
    private val paddle: PaddleOcrEngine
) : OcrEngine {

    override val isInitialized: Boolean
        get() = mlKit.isInitialized && paddle.isInitialized

    override suspend fun init() {
        coroutineScope {
            launch { mlKit.init() }
            launch { paddle.init() }
        }
    }

    override suspend fun recognize(bitmap: Bitmap): Result<List<OcrBlock>> {
        val fast = mlKit.recognize(bitmap)
        fast.onSuccess { result ->
            val needFallback = result.isEmpty() || result.sumOf { it.text.length } < 5
            if (!needFallback) return fast
        }
        val slow = paddle.recognize(bitmap)
        return slow
    }

    override fun release() {
        mlKit.release()
        paddle.release()
    }
}