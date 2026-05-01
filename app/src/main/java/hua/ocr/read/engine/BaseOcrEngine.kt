package hua.ocr.read.engine

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class BaseOcrEngine : OcrEngine {

    @Volatile
    private var _isInitialized = false

    override val isInitialized: Boolean
        get() = _isInitialized

    private val mutex = Mutex()

    override suspend fun init() {
        if (_isInitialized) return

        mutex.withLock {
            if (_isInitialized) return
            _isInitialized = onInit()
        }
    }

    protected abstract suspend fun onInit(): Boolean
}