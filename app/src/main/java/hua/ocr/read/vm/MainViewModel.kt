package hua.ocr.read.vm

import android.app.Application
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import hua.ocr.read.bean.OcrBlock
import hua.ocr.read.bean.OcrUiState
import hua.ocr.read.engine.OcrEngine
import hua.ocr.read.engine.OcrEngineType
import hua.ocr.read.engine.SmartOcrEngine
import hua.ocr.read.engine.mk_lit.MlKitOcrEngine
import hua.ocr.read.engine.paddle.PaddleOcrEngine
import hua.ocr.read.utils.getBitmapFromUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uriStateFlow = MutableStateFlow(OcrUiState(null, false))
    val uriStateFlow = _uriStateFlow.asStateFlow()

    var blocks by mutableStateOf<List<OcrBlock>>(emptyList())

    // 输入文本
    var inputText by mutableStateOf("")

    // 当前页面
    var currentPage by mutableStateOf(0)

    var imageWidth by mutableStateOf(0)
    var imageHeight by mutableStateOf(0)

    private val engineOrder = listOf(
        OcrEngineType.SMART,
        OcrEngineType.PADDLE,
        OcrEngineType.ML_KIT
    )

    var currentEngineType by mutableStateOf(OcrEngineType.PADDLE)
        private set

    private val engineMap: Map<OcrEngineType, OcrEngine> by lazy {
        val mlKit = MlKitOcrEngine()
        val paddle = PaddleOcrEngine(getApplication())
        mapOf(
            OcrEngineType.ML_KIT to mlKit,
            OcrEngineType.PADDLE to paddle,
            OcrEngineType.SMART to SmartOcrEngine(mlKit, paddle)
        )
    }

    private var engine: OcrEngine = engineMap[currentEngineType]!!


    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(getApplication()) {
            tts?.language = Locale.CHINESE
        }
        viewModelScope.launch {
            engineMap.values.forEach {
                launch { it.init() }
            }
        }
    }

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun emitError(msg: String) {
        if (msg.isEmpty()) return
        viewModelScope.launch {
            _events.emit(msg)
        }
    }

    fun updateUri(uri: Uri?, needProgress: Boolean = true) {
        _uriStateFlow.update { it.copy(uri = uri) }
        if (uri == null) {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
            return
        }
        if (needProgress) {
            processImage()
        }
    }

    fun processImage() {
        val uri = uriStateFlow.value.uri ?: return
        _uriStateFlow.update { it.copy(isLoading = true) }
        val bitmap = getApplication<Application>().getBitmapFromUri(uri)
        if (bitmap == null) {
            this._uriStateFlow.update { OcrUiState() }
            emitError("获取图片失败")
            return
        }
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        viewModelScope.launch {
            val result = engine.recognize(bitmap)
            blocks = if (result.isSuccess) {
                result.getOrNull().orEmpty()
            } else {
                emitError(result.exceptionOrNull()?.message.orEmpty())
                emptyList()
            }
            _uriStateFlow.update { it.copy(isLoading = false) }
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ocr")
    }

    fun switchEngine(): String {

        val currentIndex = engineOrder.indexOf(currentEngineType)
        val nextIndex = (currentIndex + 1) % engineOrder.size

        val next = engineOrder[nextIndex]

        currentEngineType = next
        engine = engineMap[next]!!

        viewModelScope.launch {
            engine.init()
        }

        return when (next) {
            OcrEngineType.SMART -> "已切换为 智能模式"
            OcrEngineType.PADDLE -> "已切换为 Paddle OCR"
            OcrEngineType.ML_KIT -> "已切换为 ML Kit OCR"
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }

}