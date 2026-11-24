package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FarmRepository(
    private val coreDao: FarmCoreDao,
    private val opsDao: FarmOperationsDao
) {
    private fun newId(prefix: String): String = "${prefix}_${UUID.randomUUID().toString().take(8)}"

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (coreDao.countUsers() == 0) {
            SeedDataProvider.buildSeedUsers(now).forEach { coreDao.upsertUser(it) }
            SeedDataProvider.buildDemoFarms(now).forEach { coreDao.upsertFarm(it) }
            SeedDataProvider.buildDemoBarns(now).forEach { coreDao.upsertBarn(it) }
            SeedDataProvider.buildDemoAnimals(now).forEach { coreDao.upsertAnimal(it) }
            SeedDataProvider.buildDemoEvents(now).forEach { coreDao.insertAnimalEvent(it) }
            SeedDataProvider.buildDemoBreeding(now).forEach { opsDao.upsertBreedingRecord(it) }
            SeedDataProvider.buildDemoHealth(now).forEach { opsDao.upsertHealthRecord(it) }
            SeedDataProvider.buildDemoVaccinations(now).forEach { opsDao.insertVaccination(it) }
            SeedDataProvider.buildDemoDewormings(now).forEach { opsDao.insertDeworming(it) }
            SeedDataProvider.buildDemoMedications(now).forEach { opsDao.upsertMedication(it) }
            opsDao.insertFeedItems(SeedDataProvider.buildFeedLibrary())
            SeedDataProvider.buildDemoFeedInventory(now).forEach { opsDao.upsertFeedInventory(it) }
            SeedDataProvider.buildDemoFeedRations().forEach { opsDao.upsertFeedRation(it) }
            SeedDataProvider.buildDemoTasks(now).forEach { opsDao.upsertCareTask(it) }
            SeedDataProvider.buildDemoTransactions(now).forEach { opsDao.upsertTransaction(it) }
            SeedDataProvider.buildSeedNotifications(now).forEach { opsDao.upsertNotification(it) }
        }
        if (opsDao.countArticles() == 0) {
            opsDao.insertArticles(SeedDataProvider.buildKnowledgeArticles(now))
            opsDao.insertNewsList(SeedDataProvider.buildSeedNews(now))
            opsDao.insertMarketPrices(SeedDataProvider.buildSeedMarketPrices(now))
        }
    }

    // --- Auth & User ---
    fun observeUser(userId: String): Flow<UserEntity?> = coreDao.observeUser(userId)
    fun observeAllUsers(): Flow<List<UserEntity>> = coreDao.observeAllUsers()

    suspend fun login(email: String, password: String): Result<Pair<UserEntity, String?>> {
        val cleanEmail = email.trim().lowercase()
        val user = coreDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy tài khoản với email này."))
        if (!user.isActive) {
            return Result.failure(IllegalStateException("Tài khoản đã bị khóa bởi quản trị viên."))
        }
        if (!PasswordHasher.verify(password, user.salt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Mật khẩu không chính xác."))
        }
        val farms = coreDao.getFarmsForUser(user.id)
        return Result.success(user to farms.firstOrNull()?.id)
    }

    suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        farmName: String,
        speciesCsv: String
    ): Result<Pair<UserEntity, String>> {
        val cleanEmail = email.trim().lowercase()
        if (fullName.isBlank() || cleanEmail.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập họ tên, email hợp lệ và mật khẩu từ 6 ký tự."))
        }
        if (coreDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("Email này đã được đăng ký."))
        }
        val now = System.currentTimeMillis()
        val salt = PasswordHasher.generateSalt()
        val user = UserEntity(
            id = newId("usr"),
            fullName = fullName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            passwordHash = PasswordHasher.hashPassword(password, salt),
            salt = salt,
            role = "USER",
            preferredSpeciesCsv = speciesCsv.ifBlank { "Dê,Bò,Heo,Gà" },
            createdAt = now,
            updatedAt = now
        )
        val farm = FarmEntity(
            id = newId("farm"),
            ownerUserId = user.id,
            name = farmName.trim().ifBlank { "Trang trại của ${user.fullName}" },
            address = "",
            farmingType = "Chăn nuôi hộ gia đình",
            scale = "Hộ chăn nuôi nhỏ",
            createdAt = now,
            updatedAt = now
        )
        coreDao.upsertUser(user)
        coreDao.upsertFarm(farm)
        return Result.success(user to farm.id)
    }

    suspend fun resetPasswordByPhoneVerification(email: String, phone: String, newPassword: String): Result<Unit> {
        val user = coreDao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy email này."))
        if (user.phone.isNotBlank() && user.phone.trim() != phone.trim()) {
            return Result.failure(IllegalArgumentException("Số điện thoại xác minh không khớp với hồ sơ."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới cần ít nhất 6 ký tự."))
        }
        val salt = PasswordHasher.generateSalt()
        coreDao.upsertUser(
            user.copy(
                passwordHash = PasswordHasher.hashPassword(newPassword, salt),
                salt = salt,
                updatedAt = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        coreDao.upsertUser(user.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String): Result<Unit> {
        val user = coreDao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("Không tìm thấy người dùng."))
        if (!PasswordHasher.verify(oldPassword, user.salt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Mật khẩu hiện tại không đúng."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới cần ít nhất 6 ký tự."))
        }
        val salt = PasswordHasher.generateSalt()
        coreDao.upsertUser(
            user.copy(
                passwordHash = PasswordHasher.hashPassword(newPassword, salt),
                salt = salt,
                updatedAt = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateUserRoleAndStatus(userId: String, role: String, isActive: Boolean) {
        coreDao.updateUserRoleAndStatus(userId, role, isActive, System.currentTimeMillis())
    }

    // --- Farms ---
    fun observeFarmsForUser(userId: String): Flow<List<FarmEntity>> = coreDao.observeFarmsForUser(userId)
    fun observeAllFarms(): Flow<List<FarmEntity>> = coreDao.observeAllFarms()

    suspend fun saveFarm(farm: FarmEntity): FarmEntity {
        val finalFarm = if (farm.id.isBlank()) farm.copy(id = newId("farm")) else farm.copy(updatedAt = System.currentTimeMillis())
        coreDao.upsertFarm(finalFarm)
        return finalFarm
    }

    suspend fun deleteFarm(farmId: String) = coreDao.deleteFarm(farmId)

    // --- Barns ---
    fun observeBarns(farmId: String): Flow<List<BarnEntity>> = coreDao.observeActiveBarns(farmId)

    suspend fun saveBarn(barn: BarnEntity) {
        val entity = if (barn.id.isBlank()) barn.copy(id = newId("barn")) else barn.copy(updatedAt = System.currentTimeMillis())
        coreDao.upsertBarn(entity)
    }

    suspend fun markBarnCleaned(barn: BarnEntity) {
        val now = System.currentTimeMillis()
        coreDao.upsertBarn(barn.copy(lastCleanedAt = now, status = "Đang hoạt động", updatedAt = now))
    }

    suspend fun markBarnRepaired(barn: BarnEntity) {
        val now = System.currentTimeMillis()
        coreDao.upsertBarn(barn.copy(lastRepairedAt = now, status = "Đang hoạt động", updatedAt = now))
    }

    suspend fun archiveBarn(barnId: String) = coreDao.archiveBarn(barnId, System.currentTimeMillis())
    suspend fun deleteBarn(barnId: String) = coreDao.deleteBarn(barnId)

    // --- Animals & Events ---
    fun observeAnimals(farmId: String): Flow<List<AnimalEntity>> = coreDao.observeActiveAnimals(farmId)
    fun observeAnimalById(animalId: String): Flow<AnimalEntity?> = coreDao.observeAnimalById(animalId)
    fun observeAnimalEvents(animalId: String): Flow<List<AnimalEventEntity>> = coreDao.observeEventsForAnimal(animalId)
    fun observeRecentFarmEvents(farmId: String): Flow<List<AnimalEventEntity>> = coreDao.observeRecentFarmEvents(farmId)

    suspend fun saveAnimal(animal: AnimalEntity, isNew: Boolean) {
        val now = System.currentTimeMillis()
        val entity = if (isNew || animal.id.isBlank()) {
            animal.copy(id = newId("an"), createdAt = now, updatedAt = now)
        } else {
            animal.copy(updatedAt = now)
        }
        coreDao.upsertAnimal(entity)
        if (isNew) {
            coreDao.insertAnimalEvent(
                AnimalEventEntity(
                    id = newId("ev"),
                    farmId = entity.farmId,
                    animalId = entity.id,
                    eventType = "Nhập đàn",
                    title = "Thêm hồ sơ ${entity.name} (${entity.code})",
                    description = "Loài ${entity.species} • Cân nặng ${entity.weightKg} kg",
                    weightSnapshotKg = entity.weightKg,
                    eventDateMillis = entity.intakeDateMillis
                )
            )
        }
    }

    suspend fun transferAnimalToBarn(animal: AnimalEntity, targetBarn: BarnEntity) {
        val now = System.currentTimeMillis()
        coreDao.upsertAnimal(animal.copy(barnId = targetBarn.id, updatedAt = now))
        coreDao.insertAnimalEvent(
            AnimalEventEntity(
                id = newId("ev"),
                farmId = animal.farmId,
                animalId = animal.id,
                eventType = "Chuyển chuồng",
                title = "Chuyển sang ${targetBarn.name}",
                description = "Mã chuồng: ${targetBarn.code}",
                eventDateMillis = now
            )
        )
    }

    suspend fun recordWeight(animal: AnimalEntity, newWeightKg: Double, note: String) {
        val now = System.currentTimeMillis()
        coreDao.upsertAnimal(animal.copy(weightKg = newWeightKg, updatedAt = now))
        coreDao.insertAnimalEvent(
            AnimalEventEntity(
                id = newId("ev"),
                farmId = animal.farmId,
                animalId = animal.id,
                eventType = "Cân",
                title = "Cân trọng lượng mới: $newWeightKg kg",
                description = note.ifBlank { "Cập nhật cân nặng định kỳ" },
                weightSnapshotKg = newWeightKg,
                eventDateMillis = now
            )
        )
    }

    suspend fun updateAnimalStatus(animal: AnimalEntity, newStatus: String, reason: String) {
        val now = System.currentTimeMillis()
        coreDao.upsertAnimal(animal.copy(status = newStatus, updatedAt = now))
        val evType = when (newStatus) {
            "Đã bán" -> "Bán"
            "Đã xuất đàn" -> "Xuất đàn"
            "Đang điều trị" -> "Điều trị"
            else -> "Cập nhật"
        }
        coreDao.insertAnimalEvent(
            AnimalEventEntity(
                id = newId("ev"),
                farmId = animal.farmId,
                animalId = animal.id,
                eventType = evType,
                title = "Chuyển trạng thái: $newStatus",
                description = reason,
                eventDateMillis = now
            )
        )
    }

    suspend fun archiveAnimal(animalId: String) = coreDao.archiveAnimal(animalId, System.currentTimeMillis())
    suspend fun deleteAnimal(animalId: String) = coreDao.deleteAnimal(animalId)

    // --- Breeding & Birth ---
    fun observeBreedingRecords(farmId: String): Flow<List<BreedingRecordEntity>> = opsDao.observeBreedingRecords(farmId)
    fun observeBirthRecords(farmId: String): Flow<List<BirthRecordEntity>> = opsDao.observeBirthRecords(farmId)

    suspend fun saveBreedingRecord(record: BreedingRecordEntity) {
        val isNew = record.id.isBlank()
        val entity = if (isNew) record.copy(id = newId("br")) else record.copy(updatedAt = System.currentTimeMillis())
        opsDao.upsertBreedingRecord(entity)
        if (isNew) {
            coreDao.insertAnimalEvent(
                AnimalEventEntity(
                    id = newId("ev"),
                    farmId = entity.farmId,
                    animalId = entity.motherId,
                    eventType = "Phối giống",
                    title = "Ghi nhận phối giống (${entity.method})",
                    description = entity.notes,
                    eventDateMillis = entity.breedingDateMillis
                )
            )
        }
        if (entity.isPregnant && entity.status == "Đang mang thai") {
            coreDao.getAnimalById(entity.motherId)?.let { mother ->
                if (mother.status != "Đang mang thai") {
                    coreDao.upsertAnimal(mother.copy(status = "Đang mang thai", updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    suspend fun deleteBreedingRecord(id: String) = opsDao.deleteBreedingRecord(id)

    suspend fun recordBirthAndOffspring(
        birth: BirthRecordEntity,
        breedingRecord: BreedingRecordEntity?,
        createOffspringProfiles: Boolean,
        offspringGender: String
    ) {
        val now = System.currentTimeMillis()
        val savedBirth = birth.copy(id = birth.id.ifBlank { newId("birth") })
        opsDao.insertBirthRecord(savedBirth)

        if (breedingRecord != null) {
            opsDao.upsertBreedingRecord(
                breedingRecord.copy(
                    status = "Đã sinh",
                    stage = "Đã sinh con",
                    updatedAt = now
                )
            )
        }

        val mother = coreDao.getAnimalById(birth.motherId)
        if (mother != null) {
            coreDao.upsertAnimal(mother.copy(status = "Khỏe mạnh", updatedAt = now))
            coreDao.insertAnimalEvent(
                AnimalEventEntity(
                    id = newId("ev"),
                    farmId = birth.farmId,
                    animalId = mother.id,
                    eventType = "Sinh con",
                    title = "Sinh ${birth.liveCount}/${birth.totalBorn} con sống",
                    description = birth.notes,
                    eventDateMillis = birth.actualBirthDateMillis
                )
            )
            if (createOffspringProfiles && birth.liveCount > 0) {
                val codes = birth.offspringCodes.split(",").map { it.trim() }.filter { it.isNotBlank() }
                for (i in 0 until birth.liveCount) {
                    val code = codes.getOrNull(i) ?: "${mother.code}-C${i + 1}"
                    val child = AnimalEntity(
                        id = newId("an"),
                        farmId = birth.farmId,
                        barnId = mother.barnId,
                        code = code,
                        name = "Con của ${mother.name} #$code",
                        species = mother.species,
                        breed = mother.breed,
                        gender = offspringGender,
                        birthDateMillis = birth.actualBirthDateMillis,
                        weightKg = birth.avgOffspringWeightKg,
                        intakeDateMillis = birth.actualBirthDateMillis,
                        origin = "Sinh tại trại (Mẹ: ${mother.code})",
                        status = "Khỏe mạnh",
                        motherId = mother.id,
                        fatherId = breedingRecord?.fatherId
                    )
                    coreDao.upsertAnimal(child)
                    coreDao.insertAnimalEvent(
                        AnimalEventEntity(
                            id = newId("ev"),
                            farmId = birth.farmId,
                            animalId = child.id,
                            eventType = "Nhập đàn",
                            title = "Chào đời từ mẹ ${mother.name}",
                            description = "Cân nặng sơ sinh ${birth.avgOffspringWeightKg} kg",
                            weightSnapshotKg = birth.avgOffspringWeightKg,
                            eventDateMillis = birth.actualBirthDateMillis
                        )
                    )
                }
            }
        }
    }

    // --- Health, Vaccination, Deworming, Medications ---
    fun observeHealthRecords(farmId: String): Flow<List<HealthRecordEntity>> = opsDao.observeHealthRecords(farmId)
    fun observeVaccinations(farmId: String): Flow<List<VaccinationRecordEntity>> = opsDao.observeVaccinations(farmId)
    fun observeDewormings(farmId: String): Flow<List<DewormingRecordEntity>> = opsDao.observeDewormings(farmId)
    fun observeMedications(farmId: String): Flow<List<MedicationEntity>> = opsDao.observeMedications(farmId)

    suspend fun saveHealthRecord(record: HealthRecordEntity) {
        val isNew = record.id.isBlank()
        val entity = if (isNew) record.copy(id = newId("hl")) else record.copy(updatedAt = System.currentTimeMillis())
        opsDao.upsertHealthRecord(entity)
        coreDao.getAnimalById(entity.animalId)?.let { animal ->
            val targetStatus = when (entity.status) {
                "Đang điều trị" -> "Đang điều trị"
                "Đã hồi phục" -> "Khỏe mạnh"
                else -> "Cần theo dõi"
            }
            coreDao.upsertAnimal(animal.copy(status = targetStatus, updatedAt = System.currentTimeMillis()))
        }
        if (isNew) {
            coreDao.insertAnimalEvent(
                AnimalEventEntity(
                    id = newId("ev"),
                    farmId = entity.farmId,
                    animalId = entity.animalId,
                    eventType = if (entity.status == "Đang điều trị") "Điều trị" else "Bệnh",
                    title = "Hồ sơ sức khỏe: ${entity.symptoms}",
                    description = entity.vetDiagnosis.ifBlank { entity.description },
                    eventDateMillis = entity.detectedDateMillis
                )
            )
        }
    }

    suspend fun deleteHealthRecord(id: String) = opsDao.deleteHealthRecord(id)

    suspend fun addVaccination(record: VaccinationRecordEntity) {
        val entity = record.copy(id = record.id.ifBlank { newId("vac") })
        opsDao.insertVaccination(entity)
        coreDao.insertAnimalEvent(
            AnimalEventEntity(
                id = newId("ev"),
                farmId = entity.farmId,
                animalId = entity.animalId,
                eventType = "Tiêm phòng",
                title = "Tiêm phòng: ${entity.vaccineName}",
                description = entity.notes,
                eventDateMillis = entity.vaccinatedDateMillis
            )
        )
    }

    suspend fun deleteVaccination(id: String) = opsDao.deleteVaccination(id)

    suspend fun addDeworming(record: DewormingRecordEntity) {
        val entity = record.copy(id = record.id.ifBlank { newId("dew") })
        opsDao.insertDeworming(entity)
        coreDao.insertAnimalEvent(
            AnimalEventEntity(
                id = newId("ev"),
                farmId = entity.farmId,
                animalId = entity.animalId,
                eventType = "Điều trị",
                title = "Tẩy giun: ${entity.productUsed}",
                description = entity.notes,
                eventDateMillis = entity.dewormDateMillis
            )
        )
    }

    suspend fun deleteDeworming(id: String) = opsDao.deleteDeworming(id)

    suspend fun saveMedication(med: MedicationEntity) {
        val entity = if (med.id.isBlank()) med.copy(id = newId("med")) else med.copy(updatedAt = System.currentTimeMillis())
        opsDao.upsertMedication(entity)
    }

    suspend fun deleteMedication(id: String) = opsDao.deleteMedication(id)

    // --- Feed ---
    fun observeFeedLibrary(): Flow<List<FeedItemEntity>> = opsDao.observeFeedLibrary()
    fun observeFeedInventory(farmId: String): Flow<List<FeedInventoryEntity>> = opsDao.observeFeedInventory(farmId)
    fun observeFeedUsage(farmId: String): Flow<List<FeedUsageEntity>> = opsDao.observeFeedUsage(farmId)
    fun observeFeedRations(farmId: String): Flow<List<FeedRationEntity>> = opsDao.observeFeedRations(farmId)

    suspend fun saveFeedInventory(item: FeedInventoryEntity) {
        val entity = if (item.id.isBlank()) item.copy(id = newId("inv")) else item.copy(updatedAt = System.currentTimeMillis())
        opsDao.upsertFeedInventory(entity)
    }

    suspend fun deleteFeedInventory(id: String) = opsDao.deleteFeedInventory(id)

    suspend fun addFeedUsage(usage: FeedUsageEntity) {
        opsDao.insertFeedUsage(usage.copy(id = usage.id.ifBlank { newId("fdu") }))
    }

    suspend fun saveFeedRation(ration: FeedRationEntity) {
        opsDao.upsertFeedRation(ration.copy(id = ration.id.ifBlank { newId("rat") }))
    }

    suspend fun deleteFeedRation(id: String) = opsDao.deleteFeedRation(id)

    // --- Care Tasks ---
    fun observeCareTasks(farmId: String): Flow<List<CareTaskEntity>> = opsDao.observeCareTasks(farmId)

    suspend fun saveCareTask(task: CareTaskEntity) {
        val entity = if (task.id.isBlank()) task.copy(id = newId("task")) else task.copy(updatedAt = System.currentTimeMillis())
        opsDao.upsertCareTask(entity)
    }

    suspend fun toggleTaskCompleted(task: CareTaskEntity, completed: Boolean, resultNotes: String = "") {
        val now = System.currentTimeMillis()
        opsDao.upsertCareTask(task.copy(isCompleted = completed, resultNotes = resultNotes, updatedAt = now))
        if (completed && task.repeatRule != "Không lặp") {
            val delta = when (task.repeatRule) {
                "Hằng ngày" -> 86_400_000L
                "Hằng tuần" -> 7 * 86_400_000L
                "Hằng tháng" -> 30 * 86_400_000L
                else -> 0L
            }
            if (delta > 0L) {
                opsDao.upsertCareTask(
                    task.copy(
                        id = newId("task"),
                        dueDateMillis = task.dueDateMillis + delta,
                        isCompleted = false,
                        resultNotes = "",
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }
    }

    suspend fun deleteCareTask(id: String) = opsDao.deleteCareTask(id)

    // --- Finance ---
    fun observeTransactions(farmId: String): Flow<List<FinancialTransactionEntity>> = opsDao.observeTransactions(farmId)

    suspend fun saveTransaction(tx: FinancialTransactionEntity) {
        val entity = if (tx.id.isBlank()) tx.copy(id = newId("tx")) else tx.copy(updatedAt = System.currentTimeMillis())
        opsDao.upsertTransaction(entity)
    }

    suspend fun deleteTransaction(id: String) = opsDao.deleteTransaction(id)

    // --- Knowledge, News, Market ---
    fun observePublishedArticles(): Flow<List<KnowledgeArticleEntity>> = opsDao.observePublishedArticles()
    fun observeAllArticles(): Flow<List<KnowledgeArticleEntity>> = opsDao.observeAllArticles()
    fun observeArticleById(id: String): Flow<KnowledgeArticleEntity?> = opsDao.observeArticleById(id)

    suspend fun toggleArticleBookmark(article: KnowledgeArticleEntity) {
        opsDao.upsertArticle(article.copy(isBookmarked = !article.isBookmarked))
    }

    suspend fun toggleArticleFavorite(article: KnowledgeArticleEntity) {
        opsDao.upsertArticle(article.copy(isFavorite = !article.isFavorite))
    }

    suspend fun markArticleRead(article: KnowledgeArticleEntity) {
        opsDao.upsertArticle(article.copy(lastReadAt = System.currentTimeMillis()))
    }

    suspend fun saveArticle(article: KnowledgeArticleEntity) {
        opsDao.upsertArticle(article.copy(id = article.id.ifBlank { newId("kb") }, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteArticle(id: String) = opsDao.deleteArticle(id)

    fun observeNews(): Flow<List<NewsArticleEntity>> = opsDao.observeAllNews()
    suspend fun saveNews(news: NewsArticleEntity) = opsDao.upsertNews(news.copy(id = news.id.ifBlank { newId("news") }))
    suspend fun deleteNews(id: String) = opsDao.deleteNews(id)

    fun observeMarketPrices(): Flow<List<MarketPriceEntity>> = opsDao.observeAllMarketPrices()
    suspend fun saveMarketPrice(price: MarketPriceEntity) = opsDao.upsertMarketPrice(price.copy(id = price.id.ifBlank { newId("mp") }, updatedAt = System.currentTimeMillis()))
    suspend fun deleteMarketPrice(id: String) = opsDao.deleteMarketPrice(id)

    // --- Notifications ---
    fun observeNotifications(farmId: String): Flow<List<NotificationEntity>> = opsDao.observeNotifications(farmId)
    suspend fun markNotificationRead(id: String) = opsDao.markNotificationRead(id)
    suspend fun markAllNotificationsRead(farmId: String) = opsDao.markAllNotificationsRead(farmId)
    suspend fun deleteNotification(id: String) = opsDao.deleteNotification(id)

    suspend fun createNotification(farmId: String, title: String, message: String, category: String, targetRoute: String) {
        opsDao.upsertNotification(
            NotificationEntity(
                id = newId("notif"),
                farmId = farmId,
                title = title,
                message = message,
                category = category,
                targetRoute = targetRoute
            )
        )
    }

    // --- Chat & Feedback ---
    fun observeChatSessions(userId: String): Flow<List<ChatSessionEntity>> = opsDao.observeChatSessions(userId)
    fun observeChatMessages(sessionId: String): Flow<List<ChatMessageEntity>> = opsDao.observeChatMessages(sessionId)

    suspend fun createChatSession(userId: String, title: String): ChatSessionEntity {
        val session = ChatSessionEntity(id = newId("chat"), userId = userId, title = title)
        opsDao.upsertChatSession(session)
        return session
    }

    suspend fun insertChatMessage(msg: ChatMessageEntity) {
        opsDao.insertChatMessage(msg.copy(id = msg.id.ifBlank { newId("msg") }))
    }

    suspend fun clearChatSession(sessionId: String) = opsDao.clearSessionMessages(sessionId)
    suspend fun deleteChatSession(sessionId: String) = opsDao.deleteChatSession(sessionId)

    fun observeAllFeedback(): Flow<List<FeedbackEntity>> = opsDao.observeAllFeedback()
    suspend fun submitFeedback(userId: String, type: String, subject: String, message: String) {
        opsDao.insertFeedback(
            FeedbackEntity(
                id = newId("fb"),
                userId = userId,
                feedbackType = type,
                subject = subject,
                message = message
            )
        )
    }
}
