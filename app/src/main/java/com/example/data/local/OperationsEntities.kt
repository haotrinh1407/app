package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "feed_items")
data class FeedItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // Thức ăn thô xanh | Thức ăn tinh | Thức ăn bổ sung | Khoáng chất | Thức ăn chế biến | Thức ăn khác
    val description: String,
    val nutritionInfo: String,
    val suitableSpecies: String,
    val usageGuide: String,
    val storageGuide: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "feed_inventory",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"])]
)
data class FeedInventoryEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val feedName: String,
    val category: String,
    val quantity: Double,
    val unit: String = "kg",
    val unitPrice: Long = 0L,
    val intakeDateMillis: Long,
    val supplier: String = "",
    val expiryDateMillis: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "feed_usage",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"])]
)
data class FeedUsageEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val feedName: String,
    val amount: Double,
    val unit: String = "kg",
    val usageDateMillis: Long,
    val barnId: String? = null,
    val animalId: String? = null,
    val cost: Long = 0L,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "feed_rations",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"])]
)
data class FeedRationEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val species: String,
    val breed: String = "",
    val ageStage: String,
    val weightRange: String,
    val growthStage: String, // Sinh trưởng | Mang thai | Sau sinh | Vỗ béo
    val rationSummary: String,
    val dailyAmountKg: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "care_tasks",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["dueDateMillis"])]
)
data class CareTaskEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val title: String,
    val taskType: String, // Cho ăn | Vệ sinh | Kiểm tra sức khỏe | Tiêm phòng | Tẩy giun | Cân | Phối giống | Kiểm tra mang thai | Chuẩn bị sinh | Chăm sóc sau sinh | Sửa chữa chuồng
    val dueDateMillis: Long,
    val priority: String = "Trung bình", // Thấp | Trung bình | Cao
    val repeatRule: String = "Không lặp", // Không lặp | Hằng ngày | Hằng tuần | Hằng tháng
    val animalId: String? = null,
    val barnId: String? = null,
    val assignee: String = "",
    val remindBeforeMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val resultNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "financial_transactions",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["txDateMillis"]), Index(value = ["txType"])]
)
data class FinancialTransactionEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val txType: String, // EXPENSE | INCOME
    val category: String,
    val amount: Long,
    val txDateMillis: Long,
    val description: String,
    val animalId: String? = null,
    val barnId: String? = null,
    val receiptPhotoUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
