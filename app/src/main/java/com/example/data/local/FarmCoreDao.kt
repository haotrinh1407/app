package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmCoreDao {

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Query("UPDATE users SET role = :role, isActive = :isActive, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateUserRoleAndStatus(userId: String, role: String, isActive: Boolean, updatedAt: Long)

    // --- Farms ---
    @Query("SELECT * FROM farms WHERE ownerUserId = :userId ORDER BY createdAt ASC")
    fun observeFarmsForUser(userId: String): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms ORDER BY createdAt DESC")
    fun observeAllFarms(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE ownerUserId = :userId ORDER BY createdAt ASC")
    suspend fun getFarmsForUser(userId: String): List<FarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFarm(farm: FarmEntity)

    @Query("DELETE FROM farms WHERE id = :farmId")
    suspend fun deleteFarm(farmId: String)

    // --- Barns ---
    @Query("SELECT * FROM barns WHERE farmId = :farmId AND isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveBarns(farmId: String): Flow<List<BarnEntity>>

    @Query("SELECT * FROM barns WHERE farmId = :farmId ORDER BY createdAt DESC")
    fun observeAllBarns(farmId: String): Flow<List<BarnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBarn(barn: BarnEntity)

    @Query("UPDATE barns SET isArchived = 1, updatedAt = :updatedAt WHERE id = :barnId")
    suspend fun archiveBarn(barnId: String, updatedAt: Long)

    @Query("DELETE FROM barns WHERE id = :barnId")
    suspend fun deleteBarn(barnId: String)

    // --- Animals ---
    @Query("SELECT * FROM animals WHERE farmId = :farmId AND isArchived = 0 ORDER BY updatedAt DESC")
    fun observeActiveAnimals(farmId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE farmId = :farmId ORDER BY updatedAt DESC")
    fun observeAllAnimals(farmId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    fun observeAnimalById(animalId: String): Flow<AnimalEntity?>

    @Query("SELECT * FROM animals WHERE id = :animalId LIMIT 1")
    suspend fun getAnimalById(animalId: String): AnimalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnimal(animal: AnimalEntity)

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Query("UPDATE animals SET isArchived = 1, updatedAt = :updatedAt WHERE id = :animalId")
    suspend fun archiveAnimal(animalId: String, updatedAt: Long)

    @Query("DELETE FROM animals WHERE id = :animalId")
    suspend fun deleteAnimal(animalId: String)

    // --- Animal Events ---
    @Query("SELECT * FROM animal_events WHERE animalId = :animalId ORDER BY eventDateMillis DESC")
    fun observeEventsForAnimal(animalId: String): Flow<List<AnimalEventEntity>>

    @Query("SELECT * FROM animal_events WHERE farmId = :farmId ORDER BY eventDateMillis DESC LIMIT 20")
    fun observeRecentFarmEvents(farmId: String): Flow<List<AnimalEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimalEvent(event: AnimalEventEntity)
}
