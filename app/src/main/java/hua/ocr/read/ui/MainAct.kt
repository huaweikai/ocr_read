package hua.ocr.read.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import hua.ocr.read.vm.OCRReadViewModel

class MainAct : AppCompatActivity() {

    private val viewModel: OCRReadViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        viewModel.uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
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
        viewModel.uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
    }
}

@Composable
fun OcrHost(modifier: Modifier) {
    val navHostController = rememberNavController()
    NavHost(modifier = modifier, navController = navHostController, startDestination = "home") {
        composable("home") {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current as AppCompatActivity
    val viewModel: OCRReadViewModel by context.viewModels()
    val screenHeight = LocalWindowInfo.current.containerDpSize.height

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> viewModel.uri = uri }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.uri == null) {
            // 未选择图片：显示占位点击区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, "Select", modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("点击上传图片识别内容", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        } else {
            // 已选择图片：预览 + 操作
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 1. 图片预览（屏幕一半高度）
                Box(modifier = Modifier.fillMaxWidth().height(screenHeight / 2)) {
                    AsyncImage(
                        model = viewModel.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    // 移除按钮
                    IconButton(
                        onClick = { viewModel.uri = null },
                        modifier = Modifier.align(Alignment.TopEnd).background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.onError)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. 朗读控制按钮
                Button(
                    onClick = { viewModel.processImageAndRead() },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowUp, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("识别并全文朗读")
                        }
                    }
                }
            }
        }
    }
}
