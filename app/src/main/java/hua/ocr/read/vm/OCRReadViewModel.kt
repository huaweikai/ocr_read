package hua.ocr.read.vm

import android.app.Application
import android.graphics.ImageDecoder
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.decodeBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import hua.ocr.read.bean.OcrBlock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.util.Locale

class OCRReadViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var uri by mutableStateOf<Uri?>(null)
    var blocks by mutableStateOf<List<OcrBlock>>(emptyList())

    var imageWidth by mutableStateOf(0)
    var imageHeight by mutableStateOf(0)

    var isLoading by mutableStateOf(false)

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) {
            tts?.language = Locale.CHINESE
        }
    }

    fun processImage() {
        val uri = uri ?: return

        isLoading = true

        try {
            // ⚠️ 必须拿原图尺寸
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            imageWidth = bitmap.width
            imageHeight = bitmap.height

            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { result ->

                    blocks = result.textBlocks.mapNotNull { block ->
                        val pts = block.cornerPoints
                        if (pts != null && pts.size == 4) {
                            OcrBlock(block.text, pts.toList())
                        } else null
                    }

                    blocks = mergeBlocks(blocks)

                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }

        } catch (e: Exception) {
            isLoading = false
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ocr")
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        recognizer.close()
    }

    fun mergeBlocks(blocks: List<OcrBlock>): List<OcrBlock> {

        if (blocks.isEmpty()) return emptyList()

        val sorted = blocks.sortedBy { it.cornerPoints.minOf { p -> p.y } }

        val result = mutableListOf<MutableList<OcrBlock>>()

        for (block in sorted) {

            val currentTop = block.cornerPoints.minOf { it.y }
            val currentLeft = block.cornerPoints.minOf { it.x }

            val lastGroup = result.lastOrNull()

            if (lastGroup == null) {
                result.add(mutableListOf(block))
                continue
            }

            val lastBlock = lastGroup.last()

            val lastBottom = lastBlock.cornerPoints.maxOf { it.y }
            val lastLeft = lastBlock.cornerPoints.minOf { it.x }

            val verticalGap = currentTop - lastBottom
            val leftDiff = kotlin.math.abs(currentLeft - lastLeft)

            // 👇 这两个阈值可以调
            val isSameParagraph = verticalGap < 40 /**行间距 */ && leftDiff < 40         // 左对齐

            if (isSameParagraph) {
                lastGroup.add(block)
            } else {
                result.add(mutableListOf(block))
            }
        }

        // 合并 group → 新 block
        return result.map { group ->

            val text = group.joinToString("\n") { it.text }

            val allPoints = group.flatMap { it.cornerPoints }

            val left = allPoints.minOf { it.x }
            val top = allPoints.minOf { it.y }
            val right = allPoints.maxOf { it.x }
            val bottom = allPoints.maxOf { it.y }

            val mergedPoints = listOf(
                Point(left, top),
                Point(right, top),
                Point(right, bottom),
                Point(left, bottom)
            )

            OcrBlock(text, mergedPoints)
        }
    }
}