package hua.ocr.read.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import hua.ocr.read.BuildConfig
import hua.ocr.read.components.LiquidBottomTab
import hua.ocr.read.components.LiquidBottomTabs
import hua.ocr.read.vm.MainViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(vm: MainViewModel) {

    val pagerState = rememberPagerState(
        initialPage = vm.currentPage,
        pageCount = { 2 }
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { result ->
        result?.let {
            vm.uri = it
            vm.processImage()
        }
    }

    val backdrop = rememberLayerBackdrop()

    // 同步 page 状态
    LaunchedEffect(pagerState.currentPage) {
        vm.currentPage = pagerState.currentPage
    }

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collect { msg ->
            snackBarHostState.showSnackbar(msg)
        }
    }

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomTabsContent(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                backdrop,
                vm,
                pagerState
            )
        },
        topBar = {
            MainTopBar(
                currentPage = vm.currentPage,
                onSwitchEngine = {
                   Toast.makeText(context,  vm.switchEngine(), Toast.LENGTH_SHORT).show()
                },
                onClearText = {
                    vm.inputText = ""
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        Image(
            painter = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxSize()
                .layerBackdrop(backdrop),
            contentDescription = null,
        )
        Box {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> OCRScreen(backdrop)
                    1 -> TextInputPage(vm, backdrop)
                }
            }
        }
    }
}

@Composable
fun BottomTabsContent(
    modifier: Modifier,
    backdrop: Backdrop,
    vm: MainViewModel,
    pagerState: PagerState,
) {

    val list = listOf("OCR", "文本输入")

    val scope = rememberCoroutineScope()

    LiquidBottomTabs(
        selectedTabIndex = { vm.currentPage },
        onTabSelected = {
            scope.launch {
                pagerState.animateScrollToPage(it) }
            },
        backdrop = backdrop,
        tabsCount = list.size,
        modifier = modifier.systemBarsPadding()
    ) {
        list.forEachIndexed { index, title ->
            LiquidBottomTab(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            ) {
                Text(title)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    currentPage: Int,
    onSwitchEngine: () -> Unit,
    onClearText: () -> Unit
) {

    val title = if (currentPage == 0) "图片朗读" else "文本朗读"

    TopAppBar(
        title = {
            Text(title)
        },
        actions = {
            if (currentPage == 1) {
                IconButton(onClick = onClearText) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "清空"
                    )
                }
            } else {
                IconButton(onClick = onSwitchEngine) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "切换引擎"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}