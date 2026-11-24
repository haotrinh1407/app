package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "breeding_records",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["motherId"])]
)
data class BreedingRecordEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val motherId: String,
    val fatherId: String? = null,
    val breedingDateMillis: Long,
    val method: String = "Tự nhiên",
    val performer: String = "",
    val isPregnant: Boolean = false,
    val confirmedDateMillis: Long? = null,
    val gestationDays: Int = 150,
    val expectedBirthDateMillis: Long? = null,
    val stage: String = "Theo dõi sau phối",
    val status: String = "Đang theo dõi", // Đang theo dõi | Đang mang thai | Đã sinh | Không đậu
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "birth_records",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["motherId"])]
)
data class BirthRecordEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val breedingId: String? = null,
    val motherId: String,
    val actualBirthDateMillis: Long,
    val totalBorn: Int,
    val liveCount: Int,
    val deadCount: Int,
    val avgOffspringWeightKg: Double = 0.0,
    val offspringCodes: String = "",
    val motherCondition: String = "Ổn định",
    val offspringCondition: String = "Khỏe mạnh",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["animalId"])]
)
data class HealthRecordEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val animalId: String,
    val detectedDateMillis: Long,
    val symptoms: String,
    val description: String = "",
    val severity: String = "Trung bình", // Nhẹ | Trung bình | Nặng
    val reporter: String = "",
    val vetDiagnosis: String = "",
    val confirmedTreatment: String = "",
    val medicationsUsed: String = "",
    val recheckDateMillis: Long? = null,
    val status: String = "Đang theo dõi", // Đang theo dõi | Đang điều trị | Đã hồi phục
    val notes: String = "",
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "vaccination_records",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["animalId"])]
)
data class VaccinationRecordEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val animalId: String,
    val vaccineName: String,
    val vaccinatedDateMillis: Long,
    val performer: String = "",
    val nextFollowUpMillis: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deworming_records",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["animalId"])]
)
data class DewormingRecordEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val animalId: String,
    val dewormDateMillis: Long,
    val productUsed: String,
    val nextScheduleMillis: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "medications",
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
data class MedicationEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String = "lọ",
    val intakeDateMillis: Long,
    val expiryDateMillis: Long,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
