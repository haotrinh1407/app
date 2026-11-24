package com.example.data.local

object SeedDataProvider {

    const val DEMO_USER_ID = "user_demo_farmer"
    const val DEMO_ADMIN_ID = "user_demo_admin"
    const val DEMO_FARM_ID = "farm_demo_hoadong"

    private const val DAY_MS = 86_400_000L

    fun buildSeedUsers(now: Long): List<UserEntity> {
        val saltUser = "farmcare_demo_salt_01"
        val saltAdmin = "farmcare_admin_salt_01"
        return listOf(
            UserEntity(
                id = DEMO_USER_ID,
                fullName = "Nguyễn Văn An",
                email = "nongdan@farmcare.vn",
                phone = "0912345678",
                passwordHash = PasswordHasher.hashPassword("123456", saltUser),
                salt = saltUser,
                role = "USER",
                preferredSpeciesCsv = "Dê,Bò,Heo,Gà",
                createdAt = now - 30 * DAY_MS,
                updatedAt = now
            ),
            UserEntity(
                id = DEMO_ADMIN_ID,
                fullName = "Quản trị viên FarmCare",
                email = "admin@farmcare.vn",
                phone = "0988000111",
                passwordHash = PasswordHasher.hashPassword("admin123", saltAdmin),
                salt = saltAdmin,
                role = "ADMIN",
                preferredSpeciesCsv = "Dê,Bò,Heo,Gà,Gia cầm",
                createdAt = now - 30 * DAY_MS,
                updatedAt = now
            )
        )
    }

    fun buildDemoFarms(now: Long): List<FarmEntity> = listOf(
        FarmEntity(
            id = DEMO_FARM_ID,
            ownerUserId = DEMO_USER_ID,
            name = "Trang trại Hòa Đồng (Mẫu)",
            address = "Xã Tân Lập, Huyện Đồng Phú, Bình Phước",
            farmingType = "Chăn nuôi hỗn hợp (Dê, Bò, Heo, Gà)",
            scale = "Hộ chăn nuôi vừa",
            notes = "Dữ liệu mẫu giúp làm quen với các phân hệ FarmCare.",
            createdAt = now - 25 * DAY_MS,
            updatedAt = now
        ),
        FarmEntity(
            id = "farm_admin_demo",
            ownerUserId = DEMO_ADMIN_ID,
            name = "Trang trại Thực nghiệm Khuyến nông",
            address = "Gia Lâm, Hà Nội",
            farmingType = "Mô hình chuẩn an toàn sinh học",
            scale = "Trung tâm mẫu",
            notes = "Trang trại tham khảo dành cho quản trị viên.",
            createdAt = now - 25 * DAY_MS,
            updatedAt = now
        )
    )

    fun buildDemoBarns(now: Long): List<BarnEntity> = listOf(
        BarnEntity(
            id = "barn_de_01",
            farmId = DEMO_FARM_ID,
            code = "CH-DE01",
            name = "Chuồng Dê Sàn Cao A",
            barnType = "Chuồng sàn thoáng khí",
            species = "Dê",
            capacity = 25,
            location = "Khu Đông - Gần vườn cỏ voi",
            status = "Đang hoạt động",
            notes = "Sàn cách mặt đất 1.2m, có máng ăn treo ngoài.",
            lastCleanedAt = now - 2 * DAY_MS,
            createdAt = now - 20 * DAY_MS,
            updatedAt = now
        ),
        BarnEntity(
            id = "barn_bo_01",
            farmId = DEMO_FARM_ID,
            code = "CH-BO01",
            name = "Chuồng Bò Vỗ Béo B1",
            barnType = "Chuồng hở mái tôn lạnh",
            species = "Bò",
            capacity = 12,
            location = "Khu Bắc",
            status = "Đang hoạt động",
            notes = "Có đệm lót sinh học và máng uống tự động.",
            lastCleanedAt = now - 1 * DAY_MS,
            createdAt = now - 20 * DAY_MS,
            updatedAt = now
        ),
        BarnEntity(
            id = "barn_heo_01",
            farmId = DEMO_FARM_ID,
            code = "CH-HE01",
            name = "Chuồng Heo Nái & Thịt C",
            barnType = "Chuồng kín làm mát",
            species = "Heo",
            capacity = 30,
            location = "Khu Tây - Cách ly cổng",
            status = "Cần vệ sinh",
            notes = "Cần phun khử trùng định kỳ cuối tuần.",
            lastCleanedAt = now - 6 * DAY_MS,
            createdAt = now - 18 * DAY_MS,
            updatedAt = now
        ),
        BarnEntity(
            id = "barn_ga_01",
            farmId = DEMO_FARM_ID,
            code = "CH-GA01",
            name = "Khu Gà Thả Vườn D",
            barnType = "Chuồng kết hợp sân chơi",
            species = "Gà",
            capacity = 120,
            location = "Vườn điều phía Nam",
            status = "Đang hoạt động",
            notes = "Quây lưới B40, có ổ đẻ rơm khô.",
            lastCleanedAt = now - 3 * DAY_MS,
            createdAt = now - 15 * DAY_MS,
            updatedAt = now
        )
    )

    fun buildDemoAnimals(now: Long): List<AnimalEntity> = listOf(
        AnimalEntity(
            id = "an_de_01",
            farmId = DEMO_FARM_ID,
            barnId = "barn_de_01",
            code = "DE-001",
            name = "Bông (Dê Boer cái)",
            species = "Dê",
            breed = "Boer lai",
            gender = "Cái",
            birthDateMillis = now - 540 * DAY_MS,
            weightKg = 44.5,
            intakeDateMillis = now - 180 * DAY_MS,
            origin = "Trại giống Ninh Thuận",
            purchasePrice = 5_200_000L,
            status = "Đang mang thai",
            caretaker = "Anh An",
            notes = "Ăn khỏe, dự kiến sinh trong tháng tới.",
            createdAt = now - 180 * DAY_MS,
            updatedAt = now
        ),
        AnimalEntity(
            id = "an_de_02",
            farmId = DEMO_FARM_ID,
            barnId = "barn_de_01",
            code = "DE-002",
            name = "Mực (Dê Bách Thảo đực)",
            species = "Dê",
            breed = "Bách Thảo",
            gender = "Đực",
            birthDateMillis = now - 620 * DAY_MS,
            weightKg = 52.0,
            intakeDateMillis = now - 210 * DAY_MS,
            origin = "Hộ ông Tư - Đồng Nai",
            purchasePrice = 6_000_000L,
            status = "Khỏe mạnh",
            caretaker = "Anh An",
            notes = "Dê giống chủ lực của đàn.",
            createdAt = now - 210 * DAY_MS,
            updatedAt = now
        ),
        AnimalEntity(
            id = "an_bo_01",
            farmId = DEMO_FARM_ID,
            barnId = "barn_bo_01",
            code = "BO-001",
            name = "Vàng (Bò BBB)",
            species = "Bò",
            breed = "Lai Sind - BBB",
            gender = "Đực",
            birthDateMillis = now - 420 * DAY_MS,
            weightKg = 315.0,
            intakeDateMillis = now - 120 * DAY_MS,
            origin = "Hợp tác xã Ba Vì",
            purchasePrice = 21_500_000L,
            status = "Khỏe mạnh",
            caretaker = "Chị Lan",
            notes = "Tăng trọng tốt, mỗi ngày ăn 22kg cỏ voi + 2.5kg cám.",
            createdAt = now - 120 * DAY_MS,
            updatedAt = now
        ),
        AnimalEntity(
            id = "an_heo_01",
            farmId = DEMO_FARM_ID,
            barnId = "barn_heo_01",
            code = "HE-001",
            name = "Nái Đen 01",
            species = "Heo",
            breed = "Yorkshire - Landrace",
            gender = "Cái",
            birthDateMillis = now - 480 * DAY_MS,
            weightKg = 148.0,
            intakeDateMillis = now - 250 * DAY_MS,
            origin = "Trại giống Bình Dương",
            purchasePrice = 9_500_000L,
            status = "Cần theo dõi",
            caretaker = "Anh An",
            notes = "Hơi biếng ăn nhẹ từ hôm qua, đang theo dõi nhiệt độ.",
            createdAt = now - 250 * DAY_MS,
            updatedAt = now
        ),
        AnimalEntity(
            id = "an_ga_01",
            farmId = DEMO_FARM_ID,
            barnId = "barn_ga_01",
            code = "GA-L01",
            name = "Đàn Gà Ta Thả Vườn Lô 1 (65 con)",
            species = "Gà",
            breed = "Gà Ri lai",
            gender = "Hỗn hợp",
            birthDateMillis = now - 75 * DAY_MS,
            weightKg = 1.6,
            intakeDateMillis = now - 70 * DAY_MS,
            origin = "Trại giống Minh Dư",
            purchasePrice = 1_430_000L,
            status = "Khỏe mạnh",
            caretaker = "Chị Lan",
            notes = "Đã tiêm đủ vaccine Newcastle và Gumboro.",
            createdAt = now - 70 * DAY_MS,
            updatedAt = now
        )
    )

    fun buildDemoEvents(now: Long): List<AnimalEventEntity> = listOf(
        AnimalEventEntity(
            id = "ev_01",
            farmId = DEMO_FARM_ID,
            animalId = "an_de_01",
            eventType = "Phối giống",
            title = "Phối giống với DE-002 (Mực)",
            description = "Phối tự nhiên buổi sáng, ghi nhận đậu thai sau 21 ngày.",
            eventDateMillis = now - 118 * DAY_MS
        ),
        AnimalEventEntity(
            id = "ev_02",
            farmId = DEMO_FARM_ID,
            animalId = "an_bo_01",
            eventType = "Cân",
            title = "Cân định kỳ tháng 11",
            description = "Đạt 315 kg, tăng 18 kg so với tháng trước.",
            weightSnapshotKg = 315.0,
            eventDateMillis = now - 5 * DAY_MS
        ),
        AnimalEventEntity(
            id = "ev_03",
            farmId = DEMO_FARM_ID,
            animalId = "an_heo_01",
            eventType = "Bệnh",
            title = "Theo dõi giảm ăn nhẹ",
            description = "Ghi nhận ăn giảm 30% khẩu phần sáng, cần đo thân nhiệt.",
            eventDateMillis = now - 1 * DAY_MS
        )
    )

    fun buildDemoBreeding(now: Long): List<BreedingRecordEntity> = listOf(
        BreedingRecordEntity(
            id = "br_01",
            farmId = DEMO_FARM_ID,
            motherId = "an_de_01",
            fatherId = "an_de_02",
            breedingDateMillis = now - 125 * DAY_MS,
            method = "Tự nhiên",
            performer = "Anh An",
            isPregnant = true,
            confirmedDateMillis = now - 100 * DAY_MS,
            gestationDays = 150,
            expectedBirthDateMillis = now + 25 * DAY_MS,
            stage = "Tam cá nguyệt cuối — cần tăng khẩu phần tinh",
            status = "Đang mang thai",
            notes = "Chuẩn bị ô úm con non sạch sẽ trước sinh 10 ngày."
        )
    )

    fun buildDemoHealth(now: Long): List<HealthRecordEntity> = listOf(
        HealthRecordEntity(
            id = "hl_01",
            farmId = DEMO_FARM_ID,
            animalId = "an_heo_01",
            detectedDateMillis = now - 1 * DAY_MS,
            symptoms = "Ăn ít, nằm nhiều vào buổi trưa",
            description = "Nhiệt độ chuồng trưa tăng cao, heo ăn chậm hơn bình thường.",
            severity = "Nhẹ",
            reporter = "Anh An",
            vetDiagnosis = "Chưa khám thú y (đang theo dõi)",
            confirmedTreatment = "Bổ sung điện giải theo hướng dẫn bao bì, làm mát chuồng",
            medicationsUsed = "Điện giải vitamin C",
            recheckDateMillis = now + 1 * DAY_MS,
            status = "Đang theo dõi",
            notes = "Nếu sốt trên 39.5°C cần gọi bác sĩ thú y xã."
        )
    )

    fun buildDemoVaccinations(now: Long): List<VaccinationRecordEntity> = listOf(
        VaccinationRecordEntity(
            id = "vac_01",
            farmId = DEMO_FARM_ID,
            animalId = "an_bo_01",
            vaccineName = "Lở mồm long móng (LMLM)",
            vaccinatedDateMillis = now - 45 * DAY_MS,
            performer = "Thú y viên xã Tân Lập",
            nextFollowUpMillis = now + 135 * DAY_MS,
            notes = "Tiêm nhắc lại sau 6 tháng."
        ),
        VaccinationRecordEntity(
            id = "vac_02",
            farmId = DEMO_FARM_ID,
            animalId = "an_ga_01",
            vaccineName = "Newcastle + Gumboro",
            vaccinatedDateMillis = now - 35 * DAY_MS,
            performer = "Chị Lan",
            nextFollowUpMillis = now + 55 * DAY_MS,
            notes = "Nhỏ mắt/mũi đồng loạt toàn đàn."
        )
    )

    fun buildDemoDewormings(now: Long): List<DewormingRecordEntity> = listOf(
        DewormingRecordEntity(
            id = "dew_01",
            farmId = DEMO_FARM_ID,
            animalId = "an_de_01",
            dewormDateMillis = now - 60 * DAY_MS,
            productUsed = "Thuốc tẩy nội ngoại ký sinh trùng (theo chỉ định thú y)",
            nextScheduleMillis = now + 30 * DAY_MS,
            notes = "Thực hiện trước giai đoạn cuối thai kỳ."
        )
    )

    fun buildDemoMedications(now: Long): List<MedicationEntity> = listOf(
        MedicationEntity(
            id = "med_01",
            farmId = DEMO_FARM_ID,
            name = "Điện giải Vitamin C + B-Complex",
            category = "Bổ trợ - Giải nhiệt",
            quantity = 4.0,
            unit = "gói 1kg",
            intakeDateMillis = now - 15 * DAY_MS,
            expiryDateMillis = now + 240 * DAY_MS,
            notes = "Pha nước uống khi thời tiết nắng nóng hoặc sau vận chuyển."
        ),
        MedicationEntity(
            id = "med_02",
            farmId = DEMO_FARM_ID,
            name = "Dung dịch sát trùng Iodine chuồng trại",
            category = "Sát trùng - Vệ sinh",
            quantity = 2.0,
            unit = "can 5L",
            intakeDateMillis = now - 30 * DAY_MS,
            expiryDateMillis = now + 180 * DAY_MS,
            notes = "Phun định kỳ 1 lần/tuần quanh khu vực chuồng."
        )
    )

    fun buildFeedLibrary(): List<FeedItemEntity> = listOf(
        FeedItemEntity(
            id = "feed_co_voi",
            name = "Cỏ voi xanh VA06",
            category = "Thức ăn thô xanh",
            description = "Nguồn thức ăn thô xanh phổ biến, năng suất cao cho trâu, bò, dê.",
            nutritionInfo = "Đạm thô 9-11%, chất xơ cao, giàu nước và vitamin.",
            suitableSpecies = "Bò, Dê",
            usageGuide = "Băm nhỏ 2-3cm, phơi tái nhẹ 2-3 giờ trước khi cho dê ăn để giảm chướng hơi.",
            storageGuide = "Cắt dùng trong ngày hoặc ủ chua trong bao kín 21 ngày."
        ),
        FeedItemEntity(
            id = "feed_cam_ngo",
            name = "Bắp (ngô) mảnh nghiền",
            category = "Thức ăn tinh",
            description = "Nguồn năng lượng chính giúp vật nuôi vỗ béo nhanh.",
            nutritionInfo = "Năng lượng trao đổi ~3.300 kcal/kg, đạm thô 8-9%.",
            suitableSpecies = "Heo, Gà, Bò, Dê",
            usageGuide = "Phối trộn 30-50% trong khẩu phần thức ăn tinh tùy giai đoạn.",
            storageGuide = "Để nơi khô ráo trên kệ cao, tránh ẩm mốc gây độc tố Aflatoxin."
        ),
        FeedItemEntity(
            id = "feed_da_liem",
            name = "Đá liếm khoáng tổng hợp",
            category = "Khoáng chất",
            description = "Bổ sung khoáng vi lượng Ca, P, Na, Zn giúp tăng sức đề kháng và sinh sản.",
            suitableSpecies = "Bò, Dê",
            nutritionInfo = "Muối khoáng, Canxi, Phốt pho, Sắt, Kẽm, Đồng.",
            usageGuide = "Treo ngang tầm đầu trong chuồng để vật nuôi tự do liếm.",
            storageGuide = "Tránh mưa tạt hoặc nước đọng làm chảy đá."
        ),
        FeedItemEntity(
            id = "feed_men_visinh",
            name = "Men tiêu hóa & Probiotic bổ sung",
            category = "Thức ăn bổ sung",
            description = "Hỗ trợ đường ruột khỏe mạnh, giảm tiêu chảy ở con non.",
            nutritionInfo = "Lợi khuẩn Bacillus, Lactobacillus, Enzyme tiêu hóa.",
            suitableSpecies = "Heo, Gà, Dê, Bò",
            usageGuide = "Trộn đều vào thức ăn hoặc nước uống theo liều ghi trên nhãn sản phẩm.",
            storageGuide = "Đậy kín nắp sau khi mở, để nơi thoáng mát dưới 30°C."
        )
    )

    fun buildDemoFeedInventory(now: Long): List<FeedInventoryEntity> = listOf(
        FeedInventoryEntity(
            id = "inv_01",
            farmId = DEMO_FARM_ID,
            feedName = "Cám hỗn hợp heo & bò vỗ béo",
            category = "Thức ăn tinh",
            quantity = 180.0,
            unit = "kg",
            unitPrice = 13_500L,
            intakeDateMillis = now - 7 * DAY_MS,
            supplier = "Đại lý thức ăn Thành Công",
            expiryDateMillis = now + 75 * DAY_MS,
            notes = "Còn 7 bao (25kg/bao) + lẻ 5kg."
        ),
        FeedInventoryEntity(
            id = "inv_02",
            farmId = DEMO_FARM_ID,
            feedName = "Cỏ voi tươi cắt tại vườn",
            category = "Thức ăn thô xanh",
            quantity = 320.0,
            unit = "kg",
            unitPrice = 1_200L,
            intakeDateMillis = now - 1 * DAY_MS,
            supplier = "Tự trồng tại trang trại",
            notes = "Thu hoạch khu đất phía Đông."
        )
    )

    fun buildDemoFeedRations(): List<FeedRationEntity> = listOf(
        FeedRationEntity(
            id = "rat_01",
            farmId = DEMO_FARM_ID,
            species = "Bò",
            breed = "BBB / Lai Sind",
            ageStage = "12-18 tháng",
            weightRange = "280 - 350 kg",
            growthStage = "Vỗ béo",
            rationSummary = "20-25kg cỏ xanh + 2.5kg thức ăn tinh + 30g muối khoáng/ngày",
            dailyAmountKg = 25.0,
            notes = "Chia làm 2-3 bữa/ngày, nước sạch uống tự do."
        ),
        FeedRationEntity(
            id = "rat_02",
            farmId = DEMO_FARM_ID,
            species = "Dê",
            breed = "Boer lai",
            ageStage = "Trưởng thành",
            weightRange = "40 - 50 kg",
            growthStage = "Mang thai",
            rationSummary = "4-5kg lá xanh/cỏ phơi tái + 0.4kg cám ngô/cám gạo + đá liếm",
            dailyAmountKg = 5.0,
            notes = "Tăng thêm 0.15kg thức ăn tinh ở tháng cuối thai kỳ."
        )
    )

    fun buildDemoTasks(now: Long): List<CareTaskEntity> = listOf(
        CareTaskEntity(
            id = "task_01",
            farmId = DEMO_FARM_ID,
            title = "Phun sát trùng chuồng Heo C",
            taskType = "Vệ sinh",
            dueDateMillis = now + 2 * 3_600_000L,
            priority = "Cao",
            repeatRule = "Hằng tuần",
            barnId = "barn_heo_01",
            assignee = "Anh An",
            isCompleted = false
        ),
        CareTaskEntity(
            id = "task_02",
            farmId = DEMO_FARM_ID,
            title = "Kiểm tra nhiệt độ & ăn uống của Nái Đen 01",
            taskType = "Kiểm tra sức khỏe",
            dueDateMillis = now + 4 * 3_600_000L,
            priority = "Cao",
            repeatRule = "Không lặp",
            animalId = "an_heo_01",
            barnId = "barn_heo_01",
            assignee = "Anh An",
            isCompleted = false
        ),
        CareTaskEntity(
            id = "task_03",
            farmId = DEMO_FARM_ID,
            title = "Bổ sung thức ăn tinh cho Dê Bông (mang thai)",
            taskType = "Cho ăn",
            dueDateMillis = now + 1 * DAY_MS,
            priority = "Trung bình",
            repeatRule = "Hằng ngày",
            animalId = "an_de_01",
            barnId = "barn_de_01",
            assignee = "Chị Lan",
            isCompleted = false
        ),
        CareTaskEntity(
            id = "task_04",
            farmId = DEMO_FARM_ID,
            title = "Kiểm tra hệ thống máng uống chuồng Bò B1",
            taskType = "Sửa chữa chuồng",
            dueDateMillis = now - 1 * DAY_MS,
            priority = "Trung bình",
            repeatRule = "Không lặp",
            barnId = "barn_bo_01",
            assignee = "Anh An",
            isCompleted = false
        )
    )

    fun buildDemoTransactions(now: Long): List<FinancialTransactionEntity> = listOf(
        FinancialTransactionEntity(
            id = "tx_01",
            farmId = DEMO_FARM_ID,
            txType = "EXPENSE",
            category = "Thức ăn",
            amount = 2_700_000L,
            txDateMillis = now - 7 * DAY_MS,
            description = "Nhập 200kg cám hỗn hợp heo & bò",
            barnId = "barn_bo_01",
            notes = "Đại lý Thành Công"
        ),
        FinancialTransactionEntity(
            id = "tx_02",
            farmId = DEMO_FARM_ID,
            txType = "EXPENSE",
            category = "Vaccine",
            amount = 480_000L,
            txDateMillis = now - 14 * DAY_MS,
            description = "Vaccine phòng bệnh định kỳ cho đàn gà & bò",
            barnId = "barn_ga_01"
        ),
        FinancialTransactionEntity(
            id = "tx_03",
            farmId = DEMO_FARM_ID,
            txType = "INCOME",
            category = "Bán sản phẩm",
            amount = 4_250_000L,
            txDateMillis = now - 4 * DAY_MS,
            description = "Bán 25 con gà thịt lứa trước & 15 bao phân ủ hoai",
            barnId = "barn_ga_01"
        ),
        FinancialTransactionEntity(
            id = "tx_04",
            farmId = DEMO_FARM_ID,
            txType = "EXPENSE",
            category = "Điện",
            amount = 650_000L,
            txDateMillis = now - 2 * DAY_MS,
            description = "Tiền điện bơm nước & quạt làm mát chuồng tháng 11"
        )
    )

    fun buildKnowledgeArticles(now: Long): List<KnowledgeArticleEntity> = listOf(
        KnowledgeArticleEntity(
            id = "kb_01",
            title = "Kỹ thuật nuôi Dê Boer lai sinh sản hiệu quả cho hộ gia đình",
            category = "Giống vật nuôi",
            species = "Dê",
            summary = "Hướng dẫn chọn giống, chuồng sàn cao ráo và chăm sóc dê sinh sản ít bệnh.",
            content = "## 1. Chọn giống dê sinh sản\n- Chọn dê cái lông mượt, bầu vú cân đối, chân thẳng khỏe.\n- Tuổi phối giống lần đầu phù hợp từ 8–10 tháng tuổi (đạt 28–32 kg).\n\n## 2. Yêu cầu chuồng trại\n- Làm chuồng sàn cách mặt đất 1.0–1.2m giúp thông thoáng, tránh ẩm ướt gây thối móng.\n- Diện tích trung bình 1.2–1.5 m²/con.\n\n## 3. Chăm sóc hàng ngày\n- Phơi cỏ tái 2–3 giờ trước khi cho ăn để tránh chướng bụng đầy hơi.\n- Luôn có nước sạch và đá liếm khoáng trong chuồng.",
            cautionNote = "Không cho dê ăn cỏ ướt sương sớm hoặc cỏ mới phun thuốc bảo vệ thực vật.",
            authorOrSource = "Trung tâm Khuyến nông Quốc gia (Tài liệu tham khảo)",
            readMinutes = 5,
            keywords = "dê boer, giống dê, chuồng sàn, sinh sản",
            updatedAt = now - 5 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_02",
            title = "Phòng bệnh Lở mồm long móng và Tụ huyết trùng trên Bò",
            category = "Phòng bệnh",
            species = "Bò",
            summary = "Lịch tiêm phòng định kỳ và các dấu hiệu cảnh báo sớm cần gọi thú y.",
            content = "## 1. Lịch tiêm phòng khuyến cáo\n- Tiêm phòng Lở mồm long móng (LMLM) 2 lần/năm (cách nhau 6 tháng).\n- Tiêm Tụ huyết trùng định kỳ trước mùa mưa.\n\n## 2. Dấu hiệu nhận biết sớm\n- Bò sốt cao, bỏ ăn, chảy nhiều nước dãi có bọt.\n- Xuất hiện mụn nước ở vùng miệng, lưỡi và kẽ móng chân.\n\n## 3. Xử lý ban đầu\n- Cách ly ngay cá thể nghi ngờ, không vận chuyển ra ngoài.\n- Báo ngay cho cán bộ thú y địa phương để lấy mẫu và hướng dẫn điều trị.",
            cautionNote = "Không tự ý mua kháng sinh liều cao tiêm khi chưa có chẩn đoán của bác sĩ thú y.",
            authorOrSource = "Cục Thú y — Bộ NN&PTNT",
            readMinutes = 4,
            keywords = "bò, lở mồm long móng, vaccine, tụ huyết trùng",
            updatedAt = now - 7 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_03",
            title = "An toàn sinh học trong chăn nuôi Heo nông hộ",
            category = "An toàn sinh học",
            species = "Heo",
            summary = "5 nguyên tắc kiểm soát dịch bệnh từ cổng trại vào đến ô chuồng.",
            content = "## 1. Kiểm soát người và phương tiện\n- Hạn chế người lạ vào khu vực chuồng nuôi.\n- Bố trí hố sát trùng vôi bột hoặc dung dịch khử khuẩn ngay cổng.\n\n## 2. Vệ sinh chuồng & dụng cụ\n- Rửa sạch phân, chất độn mỗi ngày; phun sát trùng 1–2 lần/tuần.\n- Dùng ủng và quần áo riêng cho khu chuồng heo.\n\n## 3. Nguồn nước và thức ăn\n- Không dùng thức ăn thừa chưa nấu chín kỹ để tránh nguy cơ dịch tả heo châu Phi.",
            cautionNote = "Khi nhập heo mới phải cách ly theo dõi tối thiểu 21 ngày trước khi nhập đàn.",
            authorOrSource = "Cẩm nang An toàn sinh học Chăn nuôi",
            readMinutes = 6,
            keywords = "heo, an toàn sinh học, khử trùng, dịch tả",
            updatedAt = now - 3 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_04",
            title = "Chăm sóc Gà thả vườn giai đoạn úm 1–21 ngày tuổi",
            category = "Chăm sóc con non",
            species = "Gà",
            summary = "Nhiệt độ úm chuẩn, mật độ và lịch nhỏ vaccine cho gà con khỏe mạnh.",
            content = "## 1. Điều chỉnh nhiệt độ úm\n- Tuần 1: 32–34°C; tuần 2: 29–31°C; tuần 3: 26–28°C.\n- Quan sát đàn gà: nếu tụ lại dưới bóng đèn là thiếu nhiệt; tản ra sát vách là quá nóng.\n\n## 2. Nước uống và dinh dưỡng\n- Cho gà uống nước ấm có pha điện giải + vitamin trong 2 giờ đầu trước khi rải cám.\n\n## 3. Lịch phòng bệnh cơ bản\n- Nhỏ Lasota lần 1 lúc 3–5 ngày tuổi; Gumboro lúc 7–10 ngày tuổi.",
            cautionNote = "Giữ chất độn chuồng khô ráo hoàn toàn để tránh bệnh cầu trùng và hô hấp.",
            authorOrSource = "Viện Chăn nuôi Việt Nam",
            readMinutes = 5,
            keywords = "gà con, úm gà, vaccine, gà thả vườn",
            updatedAt = now - 2 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_05",
            title = "Phối trộn thức ăn thô xanh và thức ăn tinh hợp lý cho Dê và Bò",
            category = "Dinh dưỡng",
            species = "Bò",
            summary = "Cân đối tỷ lệ chất xơ và năng lượng giúp vật nuôi tăng trọng mà vẫn tiết kiệm chi phí.",
            content = "## 1. Nguyên tắc khẩu phần\n- Thức ăn thô xanh chiếm 60–75% lượng chất khô hàng ngày để đảm bảo hoạt động dạ cỏ.\n- Thức ăn tinh (ngô, cám gạo, khô dầu) chiếm 25–40% tùy giai đoạn vỗ béo hay mang thai.\n\n## 2. Ủ chua cỏ voi dự trữ\n- Giúp chủ động nguồn thức ăn vào mùa khô hạn, tăng độ ngon miệng và khả năng tiêu hóa.",
            cautionNote = "Thay đổi khẩu phần ăn từ từ trong 5–7 ngày để tránh rối loạn tiêu hóa.",
            authorOrSource = "Khoa Chăn nuôi — Học viện Nông nghiệp",
            readMinutes = 4,
            keywords = "dinh dưỡng, thức ăn, bò, dê, ủ chua",
            updatedAt = now - 9 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_06",
            title = "Theo dõi thai kỳ và chăm sóc Dê, Heo sau khi sinh",
            category = "Sinh sản",
            species = "Dê",
            summary = "Nhận biết dấu hiệu sắp sinh, đỡ đẻ an toàn và chăm sóc mẹ con sau sinh.",
            content = "## 1. Thời gian mang thai tham khảo\n- Dê: khoảng 148–152 ngày (~5 tháng).\n- Heo: khoảng 114 ngày (3 tháng 3 tuần 3 ngày).\n- Bò: khoảng 280–285 ngày (~9 tháng 10 ngày).\n\n## 2. Chuẩn bị trước sinh 7–10 ngày\n- Vệ sinh sạch ô chuồng đẻ, trải rơm khô hoặc lót sàn kín gió.\n\n## 3. Sau khi sinh\n- Lau sạch nhớt mũi miệng cho con non ngay lập tức.\n- Đảm bảo con non bú được sữa đầu trong 1–2 giờ đầu đời để có kháng thể tự nhiên.",
            cautionNote = "Nếu quá trình rặn đẻ kéo dài quá 2 giờ không ra con, cần liên hệ bác sĩ thú y ngay.",
            authorOrSource = "Trung tâm Khuyến nông Quốc gia",
            readMinutes = 5,
            keywords = "sinh sản, mang thai, sữa đầu, con non",
            updatedAt = now - 4 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_07",
            title = "Quản lý kho thức ăn và phòng chống nấm mốc mùa mưa",
            category = "Thức ăn",
            species = "Chung",
            summary = "Cách xếp bao cám đúng kỹ thuật và nhận biết thức ăn bị hư hỏng.",
            content = "## 1. Nguyên tắc kho chứa\n- Đặt bao cám trên pallet cách mặt đất tối thiểu 15cm và cách tường 30cm.\n- Áp dụng nguyên tắc nhập trước – xuất trước (FIFO).\n\n## 2. Nhận biết thức ăn hư hỏng\n- Cám vón cục, đổi màu xám xanh hoặc có mùi chua, mốc tuyệt đối không sử dụng.",
            cautionNote = "Độc tố nấm mốc có thể gây ngộ độc gan mãn tính và sảy thai ở vật nuôi.",
            authorOrSource = "Tài liệu Kỹ thuật Chăn nuôi FarmCare",
            readMinutes = 3,
            keywords = "kho thức ăn, bảo quản, nấm mốc",
            updatedAt = now - 6 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_08",
            title = "Thiết kế chuồng trại thoáng mát mùa nóng và kín gió mùa lạnh",
            category = "Chuồng trại",
            species = "Chung",
            summary = "Tối ưu hướng chuồng Đông Nam, mái che cách nhiệt và xử lý chất thải bằng đệm lót sinh học.",
            content = "## 1. Hướng chuồng và thông gió\n- Ưu tiên hướng Đông Nam để đón gió mát mùa hè và tránh gió bấc mùa đông.\n- Mái cao tối thiểu 2.8–3.5m giúp tản nhiệt tốt.\n\n## 2. Đệm lót sinh học\n- Giảm mùi hôi chuồng trại, tiết kiệm nước rửa chuồng và tạo nguồn phân bón hữu cơ chất lượng.",
            cautionNote = "Đảm bảo rãnh thoát nước xung quanh chuồng không bị ứ đọng vào mùa mưa.",
            authorOrSource = "Trung tâm Khuyến nông Quốc gia",
            readMinutes = 4,
            keywords = "chuồng trại, đệm lót sinh học, thông thoáng",
            updatedAt = now - 8 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_09",
            title = "Nhận biết dấu hiệu bất thường về sức khỏe vật nuôi hàng ngày",
            category = "Bệnh và sức khỏe",
            species = "Chung",
            summary = "Kiểm tra nhanh 5 chỉ số mỗi sáng: ăn uống, phân, hô hấp, dáng đi và niêm mạc.",
            content = "## 1. Quan sát khi cho ăn\n- Vật nuôi khỏe mạnh luôn nhanh nhẹn chạy đến máng ăn.\n- Cá thể đứng tách đàn, cúi đầu, bỏ ăn là dấu hiệu đầu tiên cần ghi nhận.\n\n## 2. Quan sát phân và hô hấp\n- Phân lỏng kéo dài, phân có máu hoặc thở gấp, ho khò khè cần tách riêng để theo dõi.",
            cautionNote = "Ghi chép ngay triệu chứng và ngày phát hiện để cung cấp chính xác cho bác sĩ thú y.",
            authorOrSource = "Cẩm nang Thú y Cơ sở",
            readMinutes = 4,
            keywords = "sức khỏe, triệu chứng, quan sát đàn",
            updatedAt = now - 1 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_10",
            title = "Quy trình vệ sinh sát trùng chuồng nuôi định kỳ",
            category = "Vệ sinh",
            species = "Chung",
            summary = "Các bước dọn dẹp cơ học trước khi phun thuốc sát trùng để đạt hiệu quả tối đa.",
            content = "## 1. Vệ sinh cơ học trước\n- Quét dọn sạch phân, rác hữu cơ và cọ rửa máng ăn, máng uống.\n- Thuốc sát trùng sẽ giảm tác dụng nếu bề mặt còn dính nhiều phân.\n\n## 2. Phun sát trùng\n- Pha đúng nồng độ hướng dẫn trên nhãn, phun ướt đều nền, vách chuồng và khu vực xung quanh.",
            cautionNote = "Không phun trực tiếp hóa chất nồng độ cao vào mắt hoặc thức ăn của vật nuôi.",
            authorOrSource = "Cục Thú y",
            readMinutes = 3,
            keywords = "vệ sinh, sát trùng, chuồng nuôi",
            updatedAt = now - 10 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_11",
            title = "Theo dõi tăng trưởng và ghi chép cân nặng định kỳ",
            category = "Tăng trưởng",
            species = "Bò",
            summary = "Cách đánh giá hiệu quả thức ăn thông qua biểu đồ tăng trọng hàng tháng.",
            content = "## 1. Tần suất cân đo\n- Cân định kỳ 15–30 ngày/lần đối với heo, bò vỗ béo và dê con.\n\n## 2. Đánh giá hiệu quả\n- So sánh chi phí thức ăn với mức tăng trọng để điều chỉnh khẩu phần kịp thời.",
            cautionNote = "Nên cân vào cùng một thời điểm trong ngày (trước bữa ăn sáng) để số liệu chính xác.",
            authorOrSource = "Tài liệu Kỹ thuật Chăn nuôi FarmCare",
            readMinutes = 3,
            keywords = "cân nặng, tăng trưởng, vỗ béo",
            updatedAt = now - 11 * DAY_MS
        ),
        KnowledgeArticleEntity(
            id = "kb_12",
            title = "Kỹ thuật nuôi Gà và Thủy cầm kết hợp an toàn",
            category = "Kỹ thuật chăn nuôi",
            species = "Gia cầm",
            summary = "Quản lý đàn gia cầm thả vườn theo hướng nâng cao chất lượng thịt và hạn chế dịch cúm.",
            content = "## 1. Quy hoạch khu thả\n- Tách riêng khu nuôi gà và vịt/ngan để tránh lây nhiễm chéo mầm bệnh đường hô hấp.\n\n## 2. Che chắn và tiêm phòng\n- Tiêm phòng Cúm gia cầm đầy đủ theo lịch địa phương và rào lưới ngăn chim hoang dã.",
            cautionNote = "Tuyệt đối không giấu dịch hoặc bán chạy gia cầm ốm.",
            authorOrSource = "Trung tâm Khuyến nông Quốc gia",
            readMinutes = 4,
            keywords = "gia cầm, gà, vịt, kỹ thuật chăn nuôi",
            updatedAt = now - 12 * DAY_MS
        )
    )

    fun buildSeedNews(now: Long): List<NewsArticleEntity> = listOf(
        NewsArticleEntity(
            id = "news_01",
            title = "Khuyến cáo tăng cường phòng chống dịch bệnh giao mùa cho đàn gia súc",
            category = "Dịch bệnh",
            species = "Bò",
            region = "Toàn quốc",
            summary = "Thời tiết chuyển mùa làm giảm sức đề kháng vật nuôi, cần kiểm tra lịch tiêm phòng và tiêu độc khử trùng chuồng trại.",
            content = "Nội dung bản tin mẫu do Quản trị viên cập nhật: Người chăn nuôi cần chủ động gia cố chuồng trại, giữ khô ráo nền chuồng, bổ sung vitamin C và khoáng chất, đồng thời rà soát lịch tiêm phòng Lở mồm long móng, Tụ huyết trùng cho đàn trâu bò và dê.",
            source = "Bản tin Quản trị FarmCare (Dữ liệu mẫu)",
            publishedAt = now - 2 * DAY_MS
        ),
        NewsArticleEntity(
            id = "news_02",
            title = "Mô hình nuôi dê sàn cao kết hợp trồng cỏ voi tiết kiệm 25% chi phí thức ăn",
            category = "Mô hình chăn nuôi",
            species = "Dê",
            region = "Đông Nam Bộ",
            summary = "Nhiều nông hộ chuyển sang chủ động nguồn cỏ xanh tại vườn giúp giảm phụ thuộc vào thức ăn tinh công nghiệp.",
            content = "Nội dung bản tin mẫu do Quản trị viên cập nhật: Việc dành khoảng 2.000m² đất trồng cỏ voi VA06 và cây họ đậu giúp hộ nuôi 20–30 con dê chủ động trên 80% nguồn thức ăn thô xanh quanh năm, đồng thời tận dụng phân dê ủ hoai bón ngược lại cho vườn cỏ.",
            source = "Bản tin Quản trị FarmCare (Dữ liệu mẫu)",
            publishedAt = now - 5 * DAY_MS
        )
    )

    fun buildSeedMarketPrices(now: Long): List<MarketPriceEntity> = listOf(
        MarketPriceEntity(
            id = "mp_01",
            productName = "Dê thịt (hơi)",
            priceType = "Giá vật nuôi",
            species = "Dê",
            region = "Miền Nam",
            priceVnd = 115_000L,
            unit = "đ/kg",
            source = "Quản trị viên cập nhật (Bảng giá mẫu)",
            updatedAt = now - 1 * DAY_MS
        ),
        MarketPriceEntity(
            id = "mp_02",
            productName = "Heo hơi xuất chuồng",
            priceType = "Giá vật nuôi",
            species = "Heo",
            region = "Miền Bắc",
            priceVnd = 64_000L,
            unit = "đ/kg",
            source = "Quản trị viên cập nhật (Bảng giá mẫu)",
            updatedAt = now - 1 * DAY_MS
        ),
        MarketPriceEntity(
            id = "mp_03",
            productName = "Bò thịt lai BBB",
            priceType = "Giá vật nuôi",
            species = "Bò",
            region = "Miền Trung",
            priceVnd = 82_000L,
            unit = "đ/kg",
            source = "Quản trị viên cập nhật (Bảng giá mẫu)",
            updatedAt = now - 2 * DAY_MS
        ),
        MarketPriceEntity(
            id = "mp_04",
            productName = "Gà ta thả vườn",
            priceType = "Giá sản phẩm chăn nuôi",
            species = "Gà",
            region = "Miền Nam",
            priceVnd = 95_000L,
            unit = "đ/kg",
            source = "Quản trị viên cập nhật (Bảng giá mẫu)",
            updatedAt = now - 2 * DAY_MS
        )
    )

    fun buildSeedNotifications(now: Long): List<NotificationEntity> = listOf(
        NotificationEntity(
            id = "notif_01",
            farmId = DEMO_FARM_ID,
            title = "Công việc quá hạn: Chuồng Bò B1",
            message = "Kiểm tra hệ thống máng uống chuồng Bò B1 đã quá hạn 1 ngày.",
            category = "Công việc quá hạn",
            targetRoute = "tasks",
            isRead = false,
            createdAt = now - 3_600_000L
        ),
        NotificationEntity(
            id = "notif_02",
            farmId = DEMO_FARM_ID,
            title = "Theo dõi sức khỏe: Nái Đen 01",
            message = "Có lịch tái kiểm tra sức khỏe trong ngày mai.",
            category = "Lịch kiểm tra",
            targetRoute = "health",
            isRead = false,
            createdAt = now - 7_200_000L
        ),
        NotificationEntity(
            id = "notif_03",
            farmId = DEMO_FARM_ID,
            title = "Dự kiến sinh sắp tới: Dê Bông (DE-001)",
            message = "Dự kiến sinh trong khoảng 25 ngày tới, cần chuẩn bị ô chuồng đẻ.",
            category = "Lịch dự kiến sinh",
            targetRoute = "reproduction",
            isRead = true,
            createdAt = now - DAY_MS
        )
    )
}
