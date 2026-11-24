package com.example.data.repository

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FarmConstants {
    val SPECIES_LIST = listOf("Dê", "Bò", "Heo", "Gà", "Gia cầm", "Khác")

    val ANIMAL_STATUSES = listOf(
        "Khỏe mạnh",
        "Cần theo dõi",
        "Đang điều trị",
        "Đang mang thai",
        "Đang cách ly",
        "Đã bán",
        "Đã chết",
        "Đã xuất đàn"
    )

    val GENDER_LIST = listOf("Cái", "Đực", "Hỗn hợp")

    val BARN_TYPES = listOf(
        "Chuồng sàn thoáng khí",
        "Chuồng hở mái tôn lạnh",
        "Chuồng kín làm mát",
        "Chuồng kết hợp sân chơi",
        "Chuồng cách ly",
        "Chuồng đẻ / úm"
    )

    val BARN_STATUSES = listOf(
        "Đang hoạt động",
        "Cần vệ sinh",
        "Đang sửa chữa",
        "Trống"
    )

    val TASK_TYPES = listOf(
        "Cho ăn",
        "Vệ sinh",
        "Kiểm tra sức khỏe",
        "Tiêm phòng",
        "Tẩy giun",
        "Cân",
        "Phối giống",
        "Kiểm tra mang thai",
        "Chuẩn bị sinh",
        "Chăm sóc sau sinh",
        "Sửa chữa chuồng"
    )

    val TASK_PRIORITIES = listOf("Cao", "Trung bình", "Thấp")
    val REPEAT_RULES = listOf("Không lặp", "Hằng ngày", "Hằng tuần", "Hằng tháng")

    val EXPENSE_CATEGORIES = listOf(
        "Mua vật nuôi",
        "Thức ăn",
        "Thuốc",
        "Vaccine",
        "Nhân công",
        "Điện",
        "Nước",
        "Chuồng trại",
        "Vận chuyển",
        "Chi phí khác"
    )

    val INCOME_CATEGORIES = listOf(
        "Bán vật nuôi",
        "Bán con giống",
        "Bán sản phẩm",
        "Bán phân",
        "Doanh thu khác"
    )

    val FEED_CATEGORIES = listOf(
        "Thức ăn thô xanh",
        "Thức ăn tinh",
        "Thức ăn bổ sung",
        "Khoáng chất",
        "Thức ăn chế biến",
        "Thức ăn khác"
    )

    val KNOWLEDGE_CATEGORIES = listOf(
        "Giống vật nuôi",
        "Bệnh và sức khỏe",
        "Dinh dưỡng",
        "Thức ăn",
        "Sinh sản",
        "Chuồng trại",
        "Kỹ thuật chăn nuôi",
        "Phòng bệnh",
        "Chăm sóc con non",
        "Tăng trưởng",
        "Vệ sinh",
        "An toàn sinh học"
    )

    val NEWS_CATEGORIES = listOf(
        "Tin chăn nuôi",
        "Dịch bệnh",
        "Chính sách",
        "Kỹ thuật",
        "Mô hình chăn nuôi",
        "Kinh nghiệm",
        "Công nghệ",
        "An toàn sinh học"
    )

    val MARKET_PRICE_TYPES = listOf(
        "Giá vật nuôi",
        "Giá con giống",
        "Giá thức ăn",
        "Giá sản phẩm chăn nuôi"
    )

    val REGIONS = listOf("Toàn quốc", "Miền Bắc", "Miền Trung", "Miền Nam", "Đông Nam Bộ", "Đồng bằng sông Cửu Long")

    fun gestationDaysForSpecies(species: String): Int = when (species) {
        "Dê" -> 150
        "Bò" -> 283
        "Heo" -> 114
        "Gà", "Gia cầm" -> 21
        else -> 150
    }
}

object FarmFormatters {
    private val viLocale = Locale("vi", "VN")
    private val currencyFormat = NumberFormat.getNumberInstance(viLocale)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", viLocale)
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", viLocale)
    private val monthYearFormat = SimpleDateFormat("MM/yyyy", viLocale)

    fun formatVnd(amount: Long): String = "${currencyFormat.format(amount)} đ"

    fun formatDate(millis: Long?): String =
        if (millis == null || millis <= 0L) "Chưa rõ" else dateFormat.format(Date(millis))

    fun formatDateTime(millis: Long?): String =
        if (millis == null || millis <= 0L) "Chưa rõ" else dateTimeFormat.format(Date(millis))

    fun formatMonthYear(millis: Long): String = monthYearFormat.format(Date(millis))

    fun formatWeight(weightKg: Double, unit: String = "kg"): String {
        return if (unit == "tấn" && weightKg >= 100.0) {
            String.format(viLocale, "%.2f tấn", weightKg / 1000.0)
        } else {
            if (weightKg % 1.0 == 0.0) "${weightKg.toInt()} kg"
            else String.format(viLocale, "%.1f kg", weightKg)
        }
    }
}
