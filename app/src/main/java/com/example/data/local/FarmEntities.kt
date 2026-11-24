package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val passwordHash: String,
    val salt: String,
    val role: String = "USER", // USER | ADMIN
    val preferredSpeciesCsv: String = "Dê,Bò,Heo,Gà",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "farms",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ownerUserId"])]
)
data class FarmEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val address: String = "",
    val farmingType: String = "Chăn nuôi tổng hợp",
    val scale: String = "Hộ gia đình",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "barns",
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
data class BarnEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val code: String,
    val name: String,
    val barnType: String,
    val species: String,
    val capacity: Int,
    val location: String = "",
    val status: String = "Đang hoạt động",
    val notes: String = "",
    val photoUri: String? = null,
    val lastCleanedAt: Long? = null,
    val lastRepairedAt: Long? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "animals",
    foreignKeys = [
        ForeignKey(
            entity = FarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["barnId"]), Index(value = ["species"]), Index(value = ["status"])]
)
data class AnimalEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val barnId: String? = null,
    val code: String,
    val name: String,
    val species: String,
    val breed: String = "",
    val gender: String = "Cái",
    val birthDateMillis: Long? = null,
    val weightKg: Double = 0.0,
    val intakeDateMillis: Long = System.currentTimeMillis(),
    val origin: String = "",
    val purchasePrice: Long = 0L,
    val status: String = "Khỏe mạnh",
    val caretaker: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val motherId: String? = null,
    val fatherId: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "animal_events",
    foreignKeys = [
        ForeignKey(
            entity = AnimalEntity::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["farmId"]), Index(value = ["animalId"])]
)
data class AnimalEventEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val animalId: String,
    val eventType: String, // Nhập đàn | Chuyển chuồng | Cân | Bệnh | Điều trị | Tiêm phòng | Phối giống | Sinh con | Bán | Xuất đàn
    val title: String,
    val description: String = "",
    val weightSnapshotKg: Double? = null,
    val eventDateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
