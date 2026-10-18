package com.example.imagegen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object StabilityClient {
    private const val URL = "https://api.stability.ai/v2beta/stable-image/generate/core"

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Возвращает байты PNG-изображения. Бросает Exception с понятным текстом при ошибке. */
    suspend fun generate(apiKey: String, prompt: String): ByteArray = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("output_format", "png")
            .addFormDataPart("aspect_ratio", "1:1")
            .build()

        val request = Request.Builder()
            .url(URL)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "image/*")
            .post(body)
            .build()

        http.newCall(request).execute().use { resp ->
            val bytes = resp.body?.bytes() ?: ByteArray(0)
            if (!resp.isSuccessful) {
                val msg = try {
                    val j = JSONObject(String(bytes))
                    j.optString("errors").ifBlank { j.optString("message", j.toString()) }
                } catch (e: Exception) {
                    "HTTP ${resp.code}"
                }
                throw Exception("Ошибка ${resp.code}: $msg")
            }
            bytes
        }
    }
}
