package hua.ocr.read.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.core.content.IntentCompat
import hua.ocr.read.vm.MainViewModel
import kotlin.getValue

class MainAct : AppCompatActivity() {

    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            AppTheme {
                MainScreen(vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            IntentCompat.getParcelableExtra<Uri>(
                intent,
                Intent.EXTRA_STREAM,
                Uri::class.java
            )?.let {
                vm.uri = it
                vm.processImage()
                vm.currentPage = 0 // 自动切到 OCR 页
            }
        } else if (intent?.action == Intent.ACTION_PROCESS_TEXT) {
            val inputText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            if (inputText.isNullOrEmpty()) return
            vm.inputText = inputText
            vm.currentPage = 1
        }
    }
}

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val materialTheme = if (isSystemInDarkTheme()) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }
    MaterialTheme(materialTheme) {
        content()
    }
}