package hua.ocr.read.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import hua.ocr.read.components.LiquidButton
import hua.ocr.read.vm.MainViewModel

@Composable
fun TextInputPage(vm: MainViewModel, backdrop: Backdrop) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = vm.inputText,
            onValueChange = { vm.inputText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("输入要朗读的内容") }
        )

        Spacer(Modifier.height(16.dp))

        LiquidButton(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                vm.speak(vm.inputText)
            },
            backdrop = backdrop,
            tint = MaterialTheme.colorScheme.primary
        ) {
            Text("播放", color = MaterialTheme.colorScheme.onPrimary)
        }

    }
}