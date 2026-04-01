package com.timeforyou.app.data.coach

import com.timeforyou.app.domain.model.CoachActivitySummary
import com.timeforyou.app.domain.model.CoachAdviceTip
import com.timeforyou.app.domain.model.CoachStructuredAdvice
import com.timeforyou.app.domain.repository.CoachAdviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OpenAiCoachAdviceRepository(
    private val apiKey: String,
    private val model: String,
    private val client: OkHttpClient,
) : CoachAdviceRepository {

    override suspend fun getAdvice(summary: CoachActivitySummary): Result<CoachStructuredAdvice> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException(CoachAdviceRepository.MISSING_API_KEY_MESSAGE),
                )
            }
            try {
                val userContent = buildUserContent(summary)
                val body = JSONObject()
                    .put("model", model)
                    .put("max_tokens", 350)
                    .put("temperature", 0.7)
                    .put("response_format", JSONObject().put("type", "json_object"))
                    .put(
                        "messages",
                        JSONArray()
                            .put(
                                JSONObject()
                                    .put("role", "system")
                                    .put("content", SYSTEM_PROMPT),
                            )
                            .put(
                                JSONObject()
                                    .put("role", "user")
                                    .put("content", userContent),
                            ),
                    )
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toString().toRequestBody(JSON_MEDIA))
                    .build()
                client.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val apiMessage = runCatching {
                            JSONObject(raw).getJSONObject("error").getString("message")
                        }.getOrNull()
                        return@withContext Result.failure(
                            IllegalStateException(
                                apiMessage ?: "Request failed (${response.code})",
                            ),
                        )
                    }
                    val text = JSONObject(raw)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                    Result.success(parseStructuredAdvice(text))
                }
            } catch (e: Exception) {
                Result.failure(
                    IllegalStateException(
                        e.message ?: "Could not reach coach service",
                        e,
                    ),
                )
            }
        }

    private fun buildUserContent(summary: CoachActivitySummary): String {
        val excerptsBlock =
            if (summary.recentNoteExcerpts.isEmpty()) {
                "(none)"
            } else {
                summary.recentNoteExcerpts.joinToString("\n") { "- $it" }
            }
        return buildString {
            appendLine("User's recent logging (only use what appears below—do not invent details):")
            appendLine("- Display name (use in \"insight\" once if not the generic placeholder \"You\"): ${summary.displayName}")
            appendLine("- Streak (consecutive days with any log): ${summary.streak}")
            appendLine("- Logs today: ${summary.todayLogCount}")
            appendLine("- Days with at least one log in last 7 days: ${summary.daysWithActivityLast7}/7")
            appendLine("- Total logs in last 7 days: ${summary.totalLogsLast7}")
            appendLine("- Typical time of day (from last-7-day timestamps): ${summary.typicalLogTimeDescription}")
            appendLine("Recent short activity labels (may be empty):")
            appendLine(excerptsBlock)
        }
    }

    private fun parseStructuredAdvice(raw: String): CoachStructuredAdvice {
        val jsonText = raw.trim().removeSurrounding("```json", "```").removeSurrounding("```").trim()
        val root = runCatching { JSONObject(jsonText) }.getOrNull()
            ?: return CoachStructuredAdvice(
                insightSummary = raw.trim(),
                tips = emptyList(),
            )
        val insightSummary = listOf(
            root.optString("insight"),
            root.optString("insight_summary"),
            root.optString("reflection"),
        ).firstOrNull { it.isNotBlank() }?.trim().orEmpty().ifEmpty { raw.trim() }
        val tips = mutableListOf<CoachAdviceTip>()
        val arr = root.optJSONArray("tips")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val title = o.optString("title").trim()
                val body = o.optString("body").trim()
                if (title.isNotEmpty() && body.isNotEmpty()) {
                    tips.add(CoachAdviceTip(title = title, body = body))
                }
            }
        }
        return CoachStructuredAdvice(insightSummary = insightSummary, tips = tips)
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private const val SYSTEM_PROMPT =
            "You coach a self-care moment-logging app. Use only the user snapshot—no invented details. " +
                "JSON shape: " +
                "\"insight\": exactly 1–2 short sentences. Naturally include their display name once " +
                "if it is not the placeholder \"You\"; name their streak (if given) and when they tend to log " +
                "(use the \"typical time of day\" line; if no logs in 7 days, say that gently). " +
                "Do not give medical or mental-health diagnoses or treatment. " +
                "\"tips\": array of 1–2 objects {title, body}. Title = the small action. Body = why this " +
                "is worth doing as real time for themselves—one short phrase on how it rests or restores " +
                "them—plus the concrete step (not generic platitudes). " +
                "Output only one JSON object with keys insight and tips. No markdown."
    }
}

class NoOpCoachAdviceRepository : CoachAdviceRepository {
    override suspend fun getAdvice(summary: CoachActivitySummary): Result<CoachStructuredAdvice> =
        Result.failure(IllegalStateException(CoachAdviceRepository.MISSING_API_KEY_MESSAGE))
}
