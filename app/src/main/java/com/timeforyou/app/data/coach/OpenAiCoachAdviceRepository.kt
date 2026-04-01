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
            appendLine("- Streak (consecutive days with any log): ${summary.streak}")
            appendLine("- Logs today: ${summary.todayLogCount}")
            appendLine(
                "- Days with at least one log in last 7 days: " +
                    "${summary.daysWithActivityLast7}/7",
            )
            appendLine("- Total logs in last 7 days: ${summary.totalLogsLast7}")
            appendLine("Recent short activity labels (may be empty):")
            appendLine(excerptsBlock)
        }
    }

    private fun parseStructuredAdvice(raw: String): CoachStructuredAdvice {
        val jsonText = raw.trim().removeSurrounding("```json", "```").removeSurrounding("```").trim()
        val root = runCatching { JSONObject(jsonText) }.getOrNull()
            ?: return CoachStructuredAdvice(
                reflection = raw.trim(),
                tips = emptyList(),
            )
        val reflection = root.optString("reflection").trim().ifEmpty { raw.trim() }
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
        return CoachStructuredAdvice(reflection = reflection, tips = tips)
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private const val SYSTEM_PROMPT =
            "You coach self-care logging. The user message is their recent activity only. " +
                "In 1 short sentence: briefly mirror their pattern using the numbers/labels " +
                "(e.g. streak, logs today, how consistent this week), then give 1–2 concrete " +
                "things to do today that fit that pattern—not generic wellness tips. " +
                "Warm and practical. No diagnoses or treatment; don't assume facts not in the data. " +
                "Put the pattern-mirror in JSON key \"reflection\". Put each action in \"tips\" as " +
                "{title, body} (1–2 items). Output only one JSON object, no markdown."
    }
}

class NoOpCoachAdviceRepository : CoachAdviceRepository {
    override suspend fun getAdvice(summary: CoachActivitySummary): Result<CoachStructuredAdvice> =
        Result.failure(IllegalStateException(CoachAdviceRepository.MISSING_API_KEY_MESSAGE))
}
