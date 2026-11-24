package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.KnowledgeArticleEntity
import kotlinx.coroutines.delay

data class AiReply(
    val text: String,
    val relatedArticles: List<KnowledgeArticleEntity>,
    val referenceSource: String,
    val isSamplePlaceholder: Boolean = true
)

/**
 * Unified AI Repository boundary.
 * When Firebase AI Logic is enabled in the future, a FirebaseAiLogicRepository implementation
 * can be swapped in behind this interface without touching ViewModels or UI.
 */
interface AiRepository {
    suspend fun askQuestion(
        question: String,
        history: List<ChatMessageEntity>,
        knowledgeBase: List<KnowledgeArticleEntity>
    ): AiReply
}

/**
 * Placeholder implementation used when Firebase is not enabled.
 * Clearly labels every response as sample/placeholder output grounded in the local Knowledge Base.
 */
class PlaceholderAiRepository : AiRepository {

    override suspend fun askQuestion(
        question: String,
        history: List<ChatMessageEntity>,
        knowledgeBase: List<KnowledgeArticleEntity>
    ): AiReply {
        delay(350) // Brief natural feedback without blocking

        val queryLower = question.lowercase()
        val matched = knowledgeBase
            .map { article ->
                val haystack = "${article.title} ${article.category} ${article.species} ${article.keywords} ${article.summary}".lowercase()
                val score = queryLower.split(" ", ",", ".", "?").count { token ->
                    token.length >= 2 && haystack.contains(token)
                }
                article to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(2)
            .ifEmpty { knowledgeBase.take(2) }

        val top = matched.firstOrNull()
        val answerBuilder = StringBuilder()
        answerBuilder.appendLine("[Phản hồi mẫu nội bộ — Cần bật Firebase AI Logic để dùng Gemini AI trực tiếp]")
        answerBuilder.appendLine()

        if (top != null) {
            answerBuilder.appendLine("Dựa trên thư viện kiến thức FarmCare về \"${top.title}\" (${top.species}):")
            answerBuilder.appendLine(top.summary)
            answerBuilder.appendLine()
            val snippet = top.content
                .lines()
                .filter { it.startsWith("-") || it.startsWith("##") }
                .take(4)
                .joinToString("\n")
            if (snippet.isNotBlank()) {
                answerBuilder.appendLine(snippet)
                answerBuilder.appendLine()
            }
            if (top.cautionNote.isNotBlank()) {
                answerBuilder.appendLine("Lưu ý an toàn: ${top.cautionNote}")
                answerBuilder.appendLine()
            }
        } else {
            answerBuilder.appendLine("Hiện chưa có bài viết phù hợp trực tiếp trong thư viện nội bộ. Bạn có thể mở mục Tra cứu để xem thêm tài liệu theo từng loài (Dê, Bò, Heo, Gà).")
            answerBuilder.appendLine()
        }

        answerBuilder.append("Khuyến cáo: Thông tin chỉ mang tính tham khảo kỹ thuật phổ thông, không tự ý kê liều thuốc và không thay thế chẩn đoán của bác sĩ thú y.")

        return AiReply(
            text = answerBuilder.toString().trim(),
            relatedArticles = matched,
            referenceSource = matched.joinToString("; ") { it.authorOrSource }.ifBlank { "Thư viện nội bộ FarmCare" },
            isSamplePlaceholder = true
        )
    }
}
