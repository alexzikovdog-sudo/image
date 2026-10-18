package com.example.imagegen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Screen(vm)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun Screen(vm: MainViewModel) {
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Генератор изображений", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = vm.apiKey,
            onValueChange = vm::onApiKeyChange,
            label = { Text("Stability AI API key") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.prompt,
            onValueChange = vm::onPromptChange,
            label = { Text("Описание картинки (лучше на английском)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = vm::generate,
            enabled = !vm.loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сгенерировать") }

        if (vm.loading) {
            CircularProgressIndicator()
        }

        vm.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        vm.image?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Результат",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(onClick = vm::save, modifier = Modifier.fillMaxWidth()) {
                Text("Сохранить в галерею")
            }
        }
    }
}
