package hua.ocr.read.engine.mk_lit

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import hua.ocr.read.bean.OcrBlock
import hua.ocr.read.engine.BaseOcrEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MlKitOcrEngine : BaseOcrEngine() {

    private val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )

    override suspend fun onInit(): Boolean {
        return true
    }

    override suspend fun recognize(bitmap: Bitmap): Result<List<OcrBlock>> {
        if (!isInitialized) {
            return Result.failure(RuntimeException("ocr engine not initialized"))
        }

        return suspendCancellableCoroutine { cont ->

            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { result ->

                    val blocks = result.textBlocks.mapNotNull {
                        val rect = it.boundingBox ?: return@mapNotNull null
                        OcrBlock(it.text, rect, 0.8f)
                    }

                    cont.resume(Result.success(mergeBlocks(blocks)))
                }
                .addOnFailureListener {
                    cont.resume(Result.failure(it))
                }
        }
    }

    override fun release() {
        recognizer.close()
    }
}