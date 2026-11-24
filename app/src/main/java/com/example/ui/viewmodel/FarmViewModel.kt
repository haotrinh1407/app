package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.data.repository.AiRepository
import com.example.data.repository.FarmRepository
import com.example.data.repository.PlaceholderAiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FarmCareDatabase.getInstance(application)
    private val repo = FarmRepository(db.farmCoreDao(), db.farmOperationsDao())
    private val prefs = UserPreferencesRepository(application)
    private val aiRepo: AiRepository = PlaceholderAiRepository()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val settings: StateFlow<UserSettings> = prefs.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val currentUser: StateFlow<UserEntity?> = settings
        .map { it.loggedInUserId }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(null) else repo.observeUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val farms: StateFlow<List<FarmEntity>> = settings
        .map { it.loggedInUserId }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeFarmsForUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFarm: StateFlow<FarmEntity?> = combine(farms, settings) { list, st ->
        list.firstOrNull { it.id == st.activeFarmId } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val activeFarmIdFlow: Flow<String> = activeFarm.map { it?.id.orEmpty() }.distinctUntilChanged()

    val barns: StateFlow<List<BarnEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeBarns(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val animals: StateFlow<List<AnimalEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeAnimals(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEvents: StateFlow<List<AnimalEventEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeRecentFarmEvents(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val breedingRecords: StateFlow<List<BreedingRecordEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeBreedingRecords(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val birthRecords: StateFlow<List<BirthRecordEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeBirthRecords(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthRecords: StateFlow<List<HealthRecordEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeHealthRecords(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaccinations: StateFlow<List<VaccinationRecordEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeVaccinations(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dewormings: StateFlow<List<DewormingRecordEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeDewormings(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<MedicationEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeMedications(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedLibrary: StateFlow<List<FeedItemEntity>> = repo.observeFeedLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedInventory: StateFlow<List<FeedInventoryEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeFeedInventory(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedUsage: StateFlow<List<FeedUsageEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeFeedUsage(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedRations: StateFlow<List<FeedRationEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeFeedRations(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val careTasks: StateFlow<List<CareTaskEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeCareTasks(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<FinancialTransactionEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeTransactions(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = activeFarmIdFlow
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeNotifications(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedArticles: StateFlow<List<KnowledgeArticleEntity>> = repo.observePublishedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArticles: StateFlow<List<KnowledgeArticleEntity>> = repo.observeAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newsArticles: StateFlow<List<NewsArticleEntity>> = repo.observeNews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketPrices: StateFlow<List<MarketPriceEntity>> = repo.observeMarketPrices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repo.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFarms: StateFlow<List<FarmEntity>> = repo.observeAllFarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeedback: StateFlow<List<FeedbackEntity>> = repo.observeAllFeedback()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Chat ---
    val chatSessions: StateFlow<List<ChatSessionEntity>> = settings
        .map { it.loggedInUserId }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeChatSessions(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatSessionId = MutableStateFlow<String?>(null)
    val activeChatSessionId: StateFlow<String?> = _activeChatSessionId.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = combine(_activeChatSessionId, chatSessions) { sid, sessions ->
        sid ?: sessions.firstOrNull()?.id
    }
        .distinctUntilChanged()
        .flatMapLatest { sid ->
            if (sid.isNullOrBlank()) flowOf(emptyList()) else repo.observeChatMessages(sid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiReplying = MutableStateFlow(false)
    val isAiReplying: StateFlow<Boolean> = _isAiReplying.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
            _isInitializing.value = false
        }
    }

    fun notify(msg: String) {
        _snackbarMessage.tryEmit(msg)
    }

    // --- Auth Actions ---
    fun login(email: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            repo.login(email, password)
                .onSuccess { (user, farmId) ->
                    prefs.setSession(user.id, farmId)
                    notify("Chào mừng ${user.fullName} quay trở lại!")
                }
                .onFailure { onError(it.message ?: "Đăng nhập thất bại.") }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        farmName: String,
        speciesCsv: String,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repo.register(fullName, email, phone, password, farmName, speciesCsv)
                .onSuccess { (user, farmId) ->
                    prefs.setSession(user.id, farmId)
                    notify("Tạo tài khoản và trang trại thành công!")
                }
                .onFailure { onError(it.message ?: "Không thể tạo tài khoản.") }
        }
    }

    fun resetPassword(email: String, phone: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repo.resetPasswordByPhoneVerification(email, phone, newPassword)
                .onSuccess { onResult(true, "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập ngay.") }
                .onFailure { onResult(false, it.message ?: "Không thể đặt lại mật khẩu.") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _activeChatSessionId.value = null
            prefs.setSession(null, null)
        }
    }

    fun updateProfile(user: UserEntity) {
        viewModelScope.launch {
            repo.updateUserProfile(user)
            notify("Đã cập nhật hồ sơ cá nhân.")
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repo.changePassword(uid, oldPass, newPass)
                .onSuccess { onResult(true, "Đổi mật khẩu thành công.") }
                .onFailure { onResult(false, it.message ?: "Đổi mật khẩu thất bại.") }
        }
    }

    // --- Preferences & Farms ---
    fun selectActiveFarm(farmId: String) {
        viewModelScope.launch { prefs.setActiveFarmId(farmId) }
    }

    fun setThemeMode(mode: String) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setLargeText(large: Boolean) = viewModelScope.launch { prefs.setLargeText(large) }
    fun setWeightUnit(unit: String) = viewModelScope.launch { prefs.setWeightUnit(unit) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }

    fun saveFarm(farm: FarmEntity) {
        viewModelScope.launch {
            val saved = repo.saveFarm(farm)
            prefs.setActiveFarmId(saved.id)
            notify("Đã lưu thông tin trang trại.")
        }
    }

    fun deleteFarm(farmId: String) {
        viewModelScope.launch {
            repo.deleteFarm(farmId)
            notify("Đã xóa trang trại.")
        }
    }

    // --- Barns & Animals ---
    fun saveBarn(barn: BarnEntity) = viewModelScope.launch {
        repo.saveBarn(barn)
        notify("Đã lưu thông tin chuồng.")
    }

    fun markBarnCleaned(barn: BarnEntity) = viewModelScope.launch {
        repo.markBarnCleaned(barn)
        notify("Đã cập nhật lịch vệ sinh ${barn.name}.")
    }

    fun markBarnRepaired(barn: BarnEntity) = viewModelScope.launch {
        repo.markBarnRepaired(barn)
        notify("Đã ghi nhận sửa chữa ${barn.name}.")
    }

    fun deleteBarn(barnId: String) = viewModelScope.launch {
        repo.deleteBarn(barnId)
        notify("Đã xóa chuồng.")
    }

    fun observeAnimal(animalId: String): Flow<AnimalEntity?> = repo.observeAnimalById(animalId)
    fun observeAnimalEvents(animalId: String): Flow<List<AnimalEventEntity>> = repo.observeAnimalEvents(animalId)

    fun saveAnimal(animal: AnimalEntity, isNew: Boolean) = viewModelScope.launch {
        repo.saveAnimal(animal, isNew)
        notify(if (isNew) "Đã thêm vật nuôi mới." else "Đã cập nhật hồ sơ vật nuôi.")
    }

    fun transferAnimal(animal: AnimalEntity, targetBarn: BarnEntity) = viewModelScope.launch {
        repo.transferAnimalToBarn(animal, targetBarn)
        notify("Đã chuyển ${animal.name} sang ${targetBarn.name}.")
    }

    fun recordWeight(animal: AnimalEntity, weightKg: Double, note: String) = viewModelScope.launch {
        repo.recordWeight(animal, weightKg, note)
        notify("Đã ghi nhận cân nặng mới.")
    }

    fun updateAnimalStatus(animal: AnimalEntity, status: String, reason: String) = viewModelScope.launch {
        repo.updateAnimalStatus(animal, status, reason)
        notify("Đã cập nhật trạng thái: $status.")
    }

    fun archiveAnimal(animalId: String) = viewModelScope.launch {
        repo.archiveAnimal(animalId)
        notify("Đã lưu trữ hồ sơ vật nuôi.")
    }

    fun deleteAnimal(animalId: String) = viewModelScope.launch {
        repo.deleteAnimal(animalId)
        notify("Đã xóa vật nuôi.")
    }

    // --- Breeding & Birth ---
    fun saveBreeding(record: BreedingRecordEntity) = viewModelScope.launch {
        repo.saveBreedingRecord(record)
        notify("Đã lưu thông tin phối giống & thai kỳ.")
    }

    fun deleteBreeding(id: String) = viewModelScope.launch {
        repo.deleteBreedingRecord(id)
        notify("Đã xóa bản ghi phối giống.")
    }

    fun recordBirth(
        birth: BirthRecordEntity,
        breedingRecord: BreedingRecordEntity?,
        createOffspring: Boolean,
        offspringGender: String
    ) = viewModelScope.launch {
        repo.recordBirthAndOffspring(birth, breedingRecord, createOffspring, offspringGender)
        notify("Đã ghi nhận ca sinh và cập nhật đàn.")
    }

    // --- Health, Vaccination, Deworming, Meds ---
    fun saveHealth(record: HealthRecordEntity) = viewModelScope.launch {
        repo.saveHealthRecord(record)
        notify("Đã lưu hồ sơ sức khỏe.")
    }

    fun deleteHealth(id: String) = viewModelScope.launch {
        repo.deleteHealthRecord(id)
        notify("Đã xóa hồ sơ sức khỏe.")
    }

    fun addVaccination(record: VaccinationRecordEntity) = viewModelScope.launch {
        repo.addVaccination(record)
        notify("Đã ghi nhận lịch tiêm phòng.")
    }

    fun deleteVaccination(id: String) = viewModelScope.launch { repo.deleteVaccination(id) }

    fun addDeworming(record: DewormingRecordEntity) = viewModelScope.launch {
        repo.addDeworming(record)
        notify("Đã ghi nhận lịch tẩy giun.")
    }

    fun deleteDeworming(id: String) = viewModelScope.launch { repo.deleteDeworming(id) }

    fun saveMedication(med: MedicationEntity) = viewModelScope.launch {
        repo.saveMedication(med)
        notify("Đã cập nhật kho thuốc.")
    }

    fun deleteMedication(id: String) = viewModelScope.launch {
        repo.deleteMedication(id)
        notify("Đã xóa thuốc khỏi kho.")
    }

    // --- Feed ---
    fun saveFeedInventory(item: FeedInventoryEntity) = viewModelScope.launch {
        repo.saveFeedInventory(item)
        notify("Đã cập nhật kho thức ăn.")
    }

    fun deleteFeedInventory(id: String) = viewModelScope.launch {
        repo.deleteFeedInventory(id)
        notify("Đã xóa mục kho thức ăn.")
    }

    fun addFeedUsage(usage: FeedUsageEntity) = viewModelScope.launch {
        repo.addFeedUsage(usage)
        notify("Đã ghi nhận tiêu thụ thức ăn.")
    }

    fun saveFeedRation(ration: FeedRationEntity) = viewModelScope.launch {
        repo.saveFeedRation(ration)
        notify("Đã lưu khẩu phần ăn.")
    }

    fun deleteFeedRation(id: String) = viewModelScope.launch { repo.deleteFeedRation(id) }

    // --- Tasks ---
    fun saveCareTask(task: CareTaskEntity) = viewModelScope.launch {
        repo.saveCareTask(task)
        notify("Đã lưu lịch chăm sóc.")
    }

    fun toggleTaskCompleted(task: CareTaskEntity, completed: Boolean, resultNotes: String = "") = viewModelScope.launch {
        repo.toggleTaskCompleted(task, completed, resultNotes)
        notify(if (completed) "Đã hoàn thành công việc." else "Đã mở lại công việc.")
    }

    fun deleteCareTask(id: String) = viewModelScope.launch {
        repo.deleteCareTask(id)
        notify("Đã xóa công việc.")
    }

    // --- Finance ---
    fun saveTransaction(tx: FinancialTransactionEntity) = viewModelScope.launch {
        repo.saveTransaction(tx)
        notify("Đã ghi nhận giao dịch tài chính.")
    }

    fun deleteTransaction(id: String) = viewModelScope.launch {
        repo.deleteTransaction(id)
        notify("Đã xóa giao dịch.")
    }

    // --- Knowledge, News, Market ---
    fun observeArticle(id: String): Flow<KnowledgeArticleEntity?> = repo.observeArticleById(id)

    fun toggleBookmark(article: KnowledgeArticleEntity) = viewModelScope.launch {
        repo.toggleArticleBookmark(article)
        notify(if (!article.isBookmarked) "Đã lưu bài viết." else "Đã bỏ lưu bài viết.")
    }

    fun toggleFavorite(article: KnowledgeArticleEntity) = viewModelScope.launch {
        repo.toggleArticleFavorite(article)
    }

    fun markArticleRead(article: KnowledgeArticleEntity) = viewModelScope.launch {
        repo.markArticleRead(article)
    }

    // --- Notifications ---
    fun markNotificationRead(id: String) = viewModelScope.launch { repo.markNotificationRead(id) }
    fun markAllNotificationsRead() = viewModelScope.launch {
        activeFarm.value?.id?.let { repo.markAllNotificationsRead(it) }
    }
    fun deleteNotification(id: String) = viewModelScope.launch { repo.deleteNotification(id) }

    // --- AI Assistant ---
    fun selectChatSession(sessionId: String) {
        _activeChatSessionId.value = sessionId
    }

    fun startNewChatSession() {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            val session = repo.createChatSession(uid, "Cuộc trò chuyện mới")
            _activeChatSessionId.value = session.id
        }
    }

    fun clearActiveChat() {
        val sid = _activeChatSessionId.value ?: chatSessions.value.firstOrNull()?.id ?: return
        viewModelScope.launch {
            repo.clearChatSession(sid)
            notify("Đã xóa lịch sử trò chuyện hiện tại.")
        }
    }

    fun sendAiQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || _isAiReplying.value) return
        val uid = currentUser.value?.id ?: return

        viewModelScope.launch {
            _isAiReplying.value = true
            try {
                val sid = _activeChatSessionId.value
                    ?: chatSessions.value.firstOrNull()?.id
                    ?: repo.createChatSession(uid, trimmed.take(36)).also {
                        _activeChatSessionId.value = it.id
                    }.id

                repo.insertChatMessage(
                    ChatMessageEntity(
                        id = "",
                        sessionId = sid,
                        role = "USER",
                        content = trimmed
                    )
                )

                val reply = aiRepo.askQuestion(
                    question = trimmed,
                    history = chatMessages.value,
                    knowledgeBase = publishedArticles.value
                )

                repo.insertChatMessage(
                    ChatMessageEntity(
                        id = "",
                        sessionId = sid,
                        role = "ASSISTANT",
                        content = reply.text,
                        relatedArticleIdsCsv = reply.relatedArticles.joinToString(",") { it.id },
                        referenceSource = reply.referenceSource,
                        isPlaceholderNotice = reply.isSamplePlaceholder
                    )
                )
            } finally {
                _isAiReplying.value = false
            }
        }
    }

    // --- Feedback & Admin ---
    fun submitFeedback(type: String, subject: String, message: String) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repo.submitFeedback(uid, type, subject, message)
            notify("Cảm ơn bạn! Phản hồi đã được ghi nhận.")
        }
    }

    fun adminSaveArticle(article: KnowledgeArticleEntity) = viewModelScope.launch {
        repo.saveArticle(article)
        notify("Đã lưu bài viết kiến thức.")
    }

    fun adminDeleteArticle(id: String) = viewModelScope.launch {
        repo.deleteArticle(id)
        notify("Đã xóa bài viết.")
    }

    fun adminSaveNews(news: NewsArticleEntity) = viewModelScope.launch {
        repo.saveNews(news)
        notify("Đã lưu tin tức.")
    }

    fun adminDeleteNews(id: String) = viewModelScope.launch {
        repo.deleteNews(id)
        notify("Đã xóa tin tức.")
    }

    fun adminSaveMarketPrice(price: MarketPriceEntity) = viewModelScope.launch {
        repo.saveMarketPrice(price)
        notify("Đã cập nhật giá thị trường.")
    }

    fun adminDeleteMarketPrice(id: String) = viewModelScope.launch {
        repo.deleteMarketPrice(id)
        notify("Đã xóa giá thị trường.")
    }

    fun adminUpdateUser(userId: String, role: String, isActive: Boolean) = viewModelScope.launch {
        repo.updateUserRoleAndStatus(userId, role, isActive)
        notify("Đã cập nhật phân quyền người dùng.")
    }

    fun buildExportSummaryText(): String {
        val farm = activeFarm.value
        val totalAnimals = animals.value.size
        val totalBarns = barns.value.size
        val totalIncome = transactions.value.filter { it.txType == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.value.filter { it.txType == "EXPENSE" }.sumOf { it.amount }
        return """
            {
              "app": "FarmCare",
              "farm": "${farm?.name ?: "N/A"}",
              "animalsCount": $totalAnimals,
              "barnsCount": $totalBarns,
              "tasksCount": ${careTasks.value.size},
              "totalIncomeVnd": $totalIncome,
              "totalExpenseVnd": $totalExpense,
              "articlesCount": ${allArticles.value.size}
            }
        """.trimIndent()
    }
}
