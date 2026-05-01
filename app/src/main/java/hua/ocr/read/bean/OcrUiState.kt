package hua.ocr.read.bean

import android.net.Uri

data class OcrUiState(
    val uri: Uri? = null,
    val isLoading: Boolean = false
)