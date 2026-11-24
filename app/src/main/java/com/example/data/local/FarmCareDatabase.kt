package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FarmEntity::class,
        BarnEntity::class,
        AnimalEntity::class,
        AnimalEventEntity::class,
        BreedingRecordEntity::class,
        BirthRecordEntity::class,
        HealthRecordEntity::class,
        VaccinationRecordEntity::class,
        DewormingRecordEntity::class,
        MedicationEntity::class,
        FeedItemEntity::class,
        FeedInventoryEntity::class,
        FeedUsageEntity::class,
        FeedRationEntity::class,
        CareTaskEntity::class,
        FinancialTransactionEntity::class,
        KnowledgeArticleEntity::class,
        NewsArticleEntity::class,
        MarketPriceEntity::class,
        NotificationEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        FeedbackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FarmCareDatabase : RoomDatabase() {
    abstract fun farmCoreDao(): FarmCoreDao
    abstract fun farmOperationsDao(): FarmOperationsDao

    companion object {
        @Volatile
        private var INSTANCE: FarmCareDatabase? = null

        fun getInstance(context: Context): FarmCareDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FarmCareDatabase::class.java,
                    "farmcare_local.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
