package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmOperationsDao {

    // --- Breeding & Birth ---
    @Query("SELECT * FROM breeding_records WHERE farmId = :farmId ORDER BY breedingDateMillis DESC")
    fun observeBreedingRecords(farmId: String): Flow<List<BreedingRecordEntity>>

    @Query("SELECT * FROM birth_records WHERE farmId = :farmId ORDER BY actualBirthDateMillis DESC")
    fun observeBirthRecords(farmId: String): Flow<List<BirthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBreedingRecord(record: BreedingRecordEntity)

    @Query("DELETE FROM breeding_records WHERE id = :id")
    suspend fun deleteBreedingRecord(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthRecord(record: BirthRecordEntity)

    // --- Health, Vaccination, Deworming, Medications ---
    @Query("SELECT * FROM health_records WHERE farmId = :farmId ORDER BY detectedDateMillis DESC")
    fun observeHealthRecords(farmId: String): Flow<List<HealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHealthRecord(record: HealthRecordEntity)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteHealthRecord(id: String)

    @Query("SELECT * FROM vaccination_records WHERE farmId = :farmId ORDER BY vaccinatedDateMillis DESC")
    fun observeVaccinations(farmId: String): Flow<List<VaccinationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(record: VaccinationRecordEntity)

    @Query("DELETE FROM vaccination_records WHERE id = :id")
    suspend fun deleteVaccination(id: String)

    @Query("SELECT * FROM deworming_records WHERE farmId = :farmId ORDER BY dewormDateMillis DESC")
    fun observeDewormings(farmId: String): Flow<List<DewormingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeworming(record: DewormingRecordEntity)

    @Query("DELETE FROM deworming_records WHERE id = :id")
    suspend fun deleteDeworming(id: String)

    @Query("SELECT * FROM medications WHERE farmId = :farmId ORDER BY expiryDateMillis ASC")
    fun observeMedications(farmId: String): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedication(med: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: String)

    // --- Feed ---
    @Query("SELECT * FROM feed_items ORDER BY name ASC")
    fun observeFeedLibrary(): Flow<List<FeedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItems(items: List<FeedItemEntity>)

    @Query("SELECT * FROM feed_inventory WHERE farmId = :farmId ORDER BY intakeDateMillis DESC")
    fun observeFeedInventory(farmId: String): Flow<List<FeedInventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeedInventory(item: FeedInventoryEntity)

    @Query("DELETE FROM feed_inventory WHERE id = :id")
    suspend fun deleteFeedInventory(id: String)

    @Query("SELECT * FROM feed_usage WHERE farmId = :farmId ORDER BY usageDateMillis DESC")
    fun observeFeedUsage(farmId: String): Flow<List<FeedUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedUsage(usage: FeedUsageEntity)

    @Query("SELECT * FROM feed_rations WHERE farmId = :farmId ORDER BY createdAt DESC")
    fun observeFeedRations(farmId: String): Flow<List<FeedRationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeedRation(ration: FeedRationEntity)

    @Query("DELETE FROM feed_rations WHERE id = :id")
    suspend fun deleteFeedRation(id: String)

    // --- Care Tasks ---
    @Query("SELECT * FROM care_tasks WHERE farmId = :farmId ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun observeCareTasks(farmId: String): Flow<List<CareTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCareTask(task: CareTaskEntity)

    @Query("DELETE FROM care_tasks WHERE id = :id")
    suspend fun deleteCareTask(id: String)

    // --- Financial Transactions ---
    @Query("SELECT * FROM financial_transactions WHERE farmId = :farmId ORDER BY txDateMillis DESC")
    fun observeTransactions(farmId: String): Flow<List<FinancialTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(tx: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    // --- Knowledge, News, Market ---
    @Query("SELECT * FROM knowledge_articles ORDER BY updatedAt DESC")
    fun observeAllArticles(): Flow<List<KnowledgeArticleEntity>>

    @Query("SELECT * FROM knowledge_articles WHERE isPublished = 1 ORDER BY updatedAt DESC")
    fun observePublishedArticles(): Flow<List<KnowledgeArticleEntity>>

    @Query("SELECT * FROM knowledge_articles WHERE id = :id LIMIT 1")
    fun observeArticleById(id: String): Flow<KnowledgeArticleEntity?>

    @Query("SELECT COUNT(*) FROM knowledge_articles")
    suspend fun countArticles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticle(article: KnowledgeArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<KnowledgeArticleEntity>)

    @Query("DELETE FROM knowledge_articles WHERE id = :id")
    suspend fun deleteArticle(id: String)

    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun observeAllNews(): Flow<List<NewsArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNews(article: NewsArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsList(items: List<NewsArticleEntity>)

    @Query("DELETE FROM news_articles WHERE id = :id")
    suspend fun deleteNews(id: String)

    @Query("SELECT * FROM market_prices ORDER BY updatedAt DESC")
    fun observeAllMarketPrices(): Flow<List<MarketPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMarketPrice(price: MarketPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrices(items: List<MarketPriceEntity>)

    @Query("DELETE FROM market_prices WHERE id = :id")
    suspend fun deleteMarketPrice(id: String)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE farmId = :farmId ORDER BY createdAt DESC")
    fun observeNotifications(farmId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE farmId = :farmId")
    suspend fun markAllNotificationsRead(farmId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    // --- Chat & Feedback ---
    @Query("SELECT * FROM chat_sessions WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeChatSessions(userId: String): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChatSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteChatSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeChatMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearSessionMessages(sessionId: String)

    @Query("SELECT * FROM feedback_items ORDER BY createdAt DESC")
    fun observeAllFeedback(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)
}
