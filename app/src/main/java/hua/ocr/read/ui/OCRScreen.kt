package hua.ocr.read.ui

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kyant.backdrop.Backdrop
import hua.ocr.read.bean.OcrBlock
import hua.ocr.read.components.LiquidButton
import hua.ocr.read.vm.MainViewModel

@Composable
fun OCRScreen(backdrop: Backdrop) {
    val viewModel: MainViewModel by (LocalActivity.current as AppCompatActivity).viewModels()

    val blocks = viewModel.blocks

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { result ->
        result?.let {
            viewModel.uri = it
            viewModel.processImage()
        }
    }

    AnimatedContent(
        targetState = viewModel.uri,
        label = "ocr_switch"
    ) { uri ->
        if (uri == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LiquidButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    onClick = { launcher.launch("image/*") },
                    backdrop = backdrop,
                    tint = MaterialTheme.colorScheme.primary
                ) {
                    Text("选择图片", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        } else {
            OCRContent(
                uri = uri,
                viewModel = viewModel,
                backdrop = backdrop,
                blocks = blocks
            )
        }
    }
}

@Composable
private fun OCRContent(
    uri: Uri,
    viewModel: MainViewModel,
    backdrop: Backdrop,
    blocks: List<OcrBlock>
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(blocks, canvasSize) {
                detectTapGestures { offset ->
                    val hit = blocks.firstOrNull { block ->
                        val mapped = mapRectToCanvas(
                            block.rect,
                            viewModel.imageWidth,
                            viewModel.imageHeight,
                            canvasSize
                        ).expand(16.dp.toPx())

                        isPointInRect(offset, mapped)
                    }

                    hit?.let { viewModel.speak(it.text) }
                }
            }
    ) {

        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            blocks.forEach { block ->
                val rect = mapRectToCanvas(
                    block.rect,
                    viewModel.imageWidth,
                    viewModel.imageHeight,
                    canvasSize
                ).expand(8.dp.toPx())

                drawRect(
                    color = Color.Red,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width(), rect.height()),
                    style = Stroke(width = 2.dp.toPx())
                )


                val path = Path().apply {
                    moveTo(rect.left, rect.top)
                    lineTo(rect.right, rect.top)
                    lineTo(rect.right, rect.bottom)
                    lineTo(rect.left, rect.bottom)
                    close()
                }

                drawPath(
                    path = path,
                    color = Color.Black.copy(alpha = 0.25f)
                )

                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        LiquidButton(
            backdrop = backdrop,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 15.dp, top = 15.dp)
                .size(48.dp),
            onClick = { viewModel.uri = null },
            tint = MaterialTheme.colorScheme.primary
        ) {
            Image(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.Default.Clear,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                contentDescription = null,
            )
        }
    }
}