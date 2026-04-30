package hua.ocr.read.ui

import androidx.activity.compose.LocalActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import hua.ocr.read.vm.OCRReadViewModel

@Composable
fun OCRScreen() {
    val viewModel: OCRReadViewModel by (LocalActivity.current as AppCompatActivity).viewModels()

    val blocks = viewModel.blocks

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(blocks, canvasSize) {

                detectTapGestures { offset ->

                    val hit = blocks.firstOrNull { block ->

                        val mapped = mapToCanvas(
                            block.cornerPoints,
                            viewModel.imageWidth,
                            viewModel.imageHeight,
                            canvasSize
                        )

                        isPointInPolygon(offset, mapped)
                    }

                    hit?.let {
                        viewModel.speak(it.text)
                    }
                }
            }
    ) {

        AsyncImage(
            model = viewModel.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.fillMaxSize()) {

            blocks.forEach { block ->

                val pts = mapToCanvas(
                    block.cornerPoints,
                    viewModel.imageWidth,
                    viewModel.imageHeight,
                    canvasSize
                )

                if (pts.size < 4) return@forEach

                val path = Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x, pts[i].y)
                    }
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
    }
}