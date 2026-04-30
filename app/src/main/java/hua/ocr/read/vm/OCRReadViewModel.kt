package hua.ocr.read.vm

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.io.IOException
import java.util.Locale

class OCRReadViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    // 状态管理
    var uri by mutableStateOf<Uri?>(null)
    var isLoading by mutableStateOf(false)

    // ML Kit 识别器 (中文)
    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    // 系统 TTS
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
    }

    fun processImageAndRead() {
        val uri = uri ?: return
        isLoading = true
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    speak(visionText.text)
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } catch (e: IOException) {
            isLoading = false
        }
    }

    private fun speak(text: String) {
        if (text.isNotBlank()) {
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ocr_node")
            result
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        recognizer.close()
    }
}