package com.example.imagegen

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var apiKey by mutableStateOf(prefs.getString("api_key", "") ?: "")
        private set
    var prompt by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var image by mutableStateOf<Bitmap?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    private var imageBytes: ByteArray? = null

    fun onApiKeyChange(v: String) {
        apiKey = v
        prefs.edit().putString("api_key", v).apply()
    }

    fun onPromptChange(v: String) {
        prompt = v
    }

    fun generate() {
        if (apiKey.isBlank()) { message = "Введите API-ключ Stability AI"; return }
        if (prompt.isBlank()) { message = "Введите описание картинки"; return }
        viewModelScope.launch {
            loading = true
            message = null
            try {
                val bytes = StabilityClient.generate(apiKey.trim(), prompt.trim())
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: throw Exception("Не удалось декодировать изображение")
                imageBytes = bytes
                image = bmp
            } catch (e: Exception) {
                message = e.message ?: "Неизвестная ошибка"
            } finally {
                loading = false
            }
        }
    }

    fun save() {
        val bytes = imageBytes ?: return
        try {
            val resolver = getApplication<Application>().contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "imagegen_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ImageGen")
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw Exception("Не удалось создать файл")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            message = "Сохранено в Pictures/ImageGen"
        } catch (e: Exception) {
            message = "Ошибка сохранения: ${e.message}"
        }
    }
}
