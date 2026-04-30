package hua.ocr.read.ui

import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.IntentCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.getValue
import coil.compose.rememberAsyncImagePainter
import hua.ocr.read.vm.OCRReadViewModel

class MainAct : AppCompatActivity() {

    private val viewModel: OCRReadViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { content ->
                OcrHost(modifier = Modifier.fillMaxSize().padding(content))
            }
        }


    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        if (intent.action == Intent.ACTION_SEND) {
            val uri = IntentCompat.getParcelableExtra<Uri>(
                intent,
                Intent.EXTRA_STREAM,
                Uri::class.java
            )
            uri?.let {
                viewModel.uri = it
                viewModel.processImage()
            }
        }
    }
}

@Composable
fun OcrHost(modifier: Modifier) {
    val navHostController = rememberNavController()
    NavHost(modifier = modifier, navController = navHostController, startDestination = "home") {
        composable("home") {
            OCRScreen()
        }
    }
}
