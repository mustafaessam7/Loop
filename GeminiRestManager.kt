package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

enum class MessageSender {
    USER, AI_ASSISTANT
}

object GeminiRestManager {
    private const val TAG = "GeminiRestManager"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("DEFAULT")) {
                key
            } else {
                // Fallback demo/public key or empty
                key.ifBlank { "" }
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun buildSystemInstruction(): JSONObject {
        return JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().apply {
                put("text", """
                    أنت "المساعد الذكي لنظام Loop لإدارة ورش صيانة السيارات والمحلات التجاريّة".
                    مهمتك مساعدة أصحاب الورش والفنيين في:
                    1. تشخيص الأعطال الميكانيكية والكهربائية للسيارات وأكواد الاعطال (OBD2).
                    2. تقديم نصائح الصيانة بأسلوب مهني ومختصر وبلهجة عراقية/عربية محترمة وودودة.
                    3. تقديم أرشادات حول جداول تبديل زيوت المحركات، الفلاتر، والإطارات.
                    4. الإجابة عن كيفية استخدام نظام Loop (إدارة الفواتير، الصندوق، الديون، والتذكيرات العاطفية).
                    
                    تنبيه هام: أجب دائماً بلغة عربية واضحة ومبسطة، وابتعد عن المصطلحات المعقدة الغامضة. إذا واجهت سؤالاً غامضاً، قدم ملخصاً مفيداً واطلب توضيحاً بسيطة.
                """.trimIndent())
            }))
        }
    }

    /**
     * Single-shot API request with robust error handling and response sanitization.
     */
    suspend fun sendMessage(
        conversationHistory: List<ChatMessage>,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("لم يتم ضبط مفتاح Gemini API. يرجى إضافة المفتاح عبر لوحة Secrets في AI Studio لتفعيل المساعد الذكي 🤖.")
            )
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Include limited recent chat history (up to last 10 messages) for context
            val recentHistory = conversationHistory.takeLast(10)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val contentObj = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
                contentsArray.put(contentObj)
            }

            // Append current prompt
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            })

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", buildSystemInstruction())
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1500)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val friendlyError = parseHttpError(response.code, responseBodyStr)
                    Log.e(TAG, "Gemini API HTTP Error ${response.code}: $responseBodyStr")
                    return@withContext Result.failure(Exception(friendlyError))
                }

                val resultText = parseGenerateContentResponse(responseBodyStr)
                if (resultText.isNotBlank()) {
                    Result.success(resultText)
                } else {
                    Result.failure(Exception("لم أتمكن من توليد إجابة مكتملة. يرجى إعادة الصياغة أو المحاولة مرة أخرى."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed", e)
            val friendlyMsg = translateNetworkOrParserException(e)
            Result.failure(Exception(friendlyMsg))
        }
    }

    /**
     * Streaming API call via Flow, safely parsing chunks line by line.
     * Prevents stream cuts, JSON errors, or raw error messages from breaking the UI.
     */
    fun sendMessageStream(
        conversationHistory: List<ChatMessage>,
        userPrompt: String
    ): Flow<String> = flow {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            emit("⚠️ يرجى ضبط مفتاح Gemini API في إعدادات النظام لتشغيل المساعد الذكي.")
            return@flow
        }

        val url = "$BASE_URL/$MODEL_NAME:streamGenerateContent?key=$apiKey&alt=sse"

        val contentsArray = JSONArray()
        val recentHistory = conversationHistory.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
            })
        }
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
        })

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", buildSystemInstruction())
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1500)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        var accumulatedText = StringBuilder()
        var hasEmittedChunk = false

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    val friendlyErr = parseHttpError(response.code, errBody)
                    emit("⚠️ $friendlyErr")
                    return@flow
                }

                val body = response.body
                if (body == null) {
                    emit("⚠️ لم يتم استقبال أي استجابة من السيرفر.")
                    return@flow
                }

                val reader = BufferedReader(InputStreamReader(body.byteStream()))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.isEmpty() || currentLine == "data: [DONE]") continue

                    val jsonStr = if (currentLine.startsWith("data:")) {
                        currentLine.substring(5).trim()
                    } else {
                        currentLine
                    }

                    if (jsonStr.isBlank() || jsonStr == "[DONE]") continue

                    // Wrap individual JSON chunk parsing in isolated try-catch to absorb stream cuts
                    try {
                        val chunkText = parseGenerateContentResponse(jsonStr)
                        if (chunkText.isNotEmpty()) {
                            accumulatedText.append(chunkText)
                            hasEmittedChunk = true
                            emit(accumulatedText.toString())
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Skipping malformed or cut stream line: $currentLine", e)
                        // Ignore truncated chunk without interrupting the stream
                    }
                }
            }

            if (!hasEmittedChunk && accumulatedText.isEmpty()) {
                emit("أهلاً بك! لم أستطع فهم الطلب بدقة، يرجى إعادة المحاولة بعبارة أخرى.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Stream error encountered", e)
            val friendlyError = translateNetworkOrParserException(e)
            if (accumulatedText.isNotEmpty()) {
                // Keep received text and append polite network warning
                emit(accumulatedText.toString() + "\n\n(تنبيه: انقطع الاتصال أثناء استلام باقي الإجابة)")
            } else {
                emit("⚠️ $friendlyError")
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Safely extracts response text from Gemini API JSON schema.
     */
    private fun parseGenerateContentResponse(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""

            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i)
                val text = part?.optString("text", "") ?: ""
                textBuilder.append(text)
            }
            textBuilder.toString()
        } catch (e: Exception) {
            // Check if response is formatted as array of JSON objects (some stream formats)
            try {
                if (jsonString.startsWith("[")) {
                    val jsonArray = JSONArray(jsonString)
                    val sb = StringBuilder()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        sb.append(parseGenerateContentResponse(item.toString()))
                    }
                    sb.toString()
                } else ""
            } catch (ex: Exception) {
                ""
            }
        }
    }

    /**
     * Sanitizes HTTP error responses and prevents raw "fax" or garbled/HTML dumps from appearing.
     */
    private fun parseHttpError(code: Int, responseBodyStr: String): String {
        val serverErrorMsg = try {
            val json = JSONObject(responseBodyStr)
            val errorObj = json.optJSONObject("error")
            errorObj?.optString("message", "") ?: ""
        } catch (e: Exception) {
            ""
        }

        return when (code) {
            429 -> "تم تجاوز حد الطلبات المسموح به للمساعد الذكي مؤقتاً (Rate Limit). يرجى الانتظار بضع ثوانٍ وإعادة المحاولة."
            400 -> "تعذر معالجة الطلب، يرجى التأكد من نص السؤال وإعادة إرساله."
            401, 403 -> "مفتاح Gemini API غير مصرح به أو صلاحيته منتهية. يرجى التحقق من الإعدادات."
            500, 502, 503 -> "خوادم المساعد الذكي مشغولة حالياً. يرجى المحاولة بعد لحظات."
            else -> if (serverErrorMsg.isNotBlank() && !serverErrorMsg.contains("fax", ignoreCase = true) && serverErrorMsg.length < 120) {
                "حدث خطأ في خدمة المساعد الذكي: $serverErrorMsg"
            } else {
                "تعذر الاتصال بالمساعد الذكي حالياً (رمز الخطأ: $code). يرجى المحاولة لاحقاً."
            }
        }
    }

    /**
     * Translates low-level network/parsing exceptions into user-friendly Arabic text.
     */
    private fun translateNetworkOrParserException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("timeout", ignoreCase = true) -> "استغرقت الاستجابة وقتاً أطول من المتوقع. يرجى التأكد من جودة الاتصال بالإنترنت."
            msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("UnknownHost", ignoreCase = true) -> "تعذر الاتصال بخادم AI. يرجى التحقق من اتصال شبكة الإنترنت."
            msg.contains("JSON", ignoreCase = true) -> "تم استلام استجابة غير مكتملة، جاري تحسين استقبال البيانات."
            else -> "حدث خطأ أثناء التواصل مع المساعد الذكي. يرجى إعادة المحاولة."
        }
    }
}
