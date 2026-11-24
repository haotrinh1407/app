package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "knowledge_articles",
    indices = [Index(value = ["category"]), Index(value = ["species"])]
)
data class KnowledgeArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val species: String,
    val summary: String,
    val content: String,
    val cautionNote: String = "",
    val authorOrSource: String,
    val readMinutes: Int = 4,
    val keywords: String = "",
    val isBookmarked: Boolean = false,
    val isFavorite: Boolean = false,
    val lastReadAt: Long? = null,
    val isPublished: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val species: String,
    val region: String,
    val summary: String,
    val content: String,
    val source: String,
    val publishedAt: Long,
    val isPublished: Boolean = true
)

@Entity(tableName = "market_prices")
data class MarketPriceEntity(
    @PrimaryKey val id: String,
    val productName: String,
    val priceType: String, // Giá vật nuôi | Giá con giống | Giá thức ăn | Giá sản phẩm chăn nuôi
    val species: String,
    val region: String,
    val priceVnd: Long,
    val unit: String,
    val source: String,
    val updatedAt: Long,
    val isPublished: Boolean = true
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["farmId"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val title: String,
    val message: String,
    val category: String,
    val targetRoute: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_sessions",
    indices = [Index(value = ["userId"])]
)
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String, // USER | ASSISTANT
    val content: String,
    val relatedArticleIdsCsv: String = "",
    val referenceSource: String = "",
    val isPlaceholderNotice: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "feedback_items")
data class FeedbackEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val feedbackType: String,
    val subject: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)
