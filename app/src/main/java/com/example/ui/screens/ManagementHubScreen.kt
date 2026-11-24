package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.repository.FarmFormatters
import com.example.ui.components.FarmCard
import com.example.ui.components.FarmTopBar
import com.example.ui.theme.FarmDanger
import com.example.ui.theme.FarmSuccess
import com.example.ui.theme.FarmWarning
import com.example.ui.viewmodel.FarmViewModel

private data class ManageModuleItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun ManagementHubScreen(
    viewModel: FarmViewModel,
    onNavigateRoute: (String) -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val barns by viewModel.barns.collectAsState()
    val breeding by viewModel.breedingRecords.collectAsState()
    val health by viewModel.healthRecords.collectAsState()
    val feedInv by viewModel.feedInventory.collectAsState()
    val tasks by viewModel.careTasks.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val pendingTasks = tasks.count { !it.isCompleted }
    val pregnantCount = breeding.count { it.status == "Đang mang thai" }
    val activeHealthCount = health.count { it.status != "Đã hồi phục" }
    val totalIncome = transactions.filter { it.txType == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.txType == "EXPENSE" }.sumOf { it.amount }
    val netProfit = totalIncome - totalExpense

    val modules = listOf(
        ManageModuleItem(
            route = "animals",
            title = "Quản lý vật nuôi",
            subtitle = "Hồ sơ chi tiết, tìm kiếm, lọc theo loài/chuồng và lịch sử sự kiện",
            badge = "${animals.size} cá thể/đàn",
            icon = Icons.Default.Pets,
            accent = MaterialTheme.colorScheme.primary
        ),
        ManageModuleItem(
            route = "barns",
            title = "Quản lý chuồng trại",
            subtitle = "Sức chứa, chuyển chuồng, lịch vệ sinh và bảo trì sửa chữa",
            badge = "${barns.size} chuồng",
            icon = Icons.Default.Warehouse,
            accent = MaterialTheme.colorScheme.secondary
        ),
        ManageModuleItem(
            route = "reproduction",
            title = "Quản lý sinh sản",
            subtitle = "Phối giống, theo dõi thai kỳ, ngày sinh dự kiến và con non",
            badge = "$pregnantCount đang mang thai",
            icon = Icons.Default.Favorite,
            accent = Color(0xFFD81B60)
        ),
        ManageModuleItem(
            route = "health",
            title = "Sức khỏe & Thuốc thú y",
            subtitle = "Timeline bệnh án, tiêm phòng, tẩy giun và kho thuốc",
            badge = "$activeHealthCount đang theo dõi",
            icon = Icons.Default.MedicalServices,
            accent = FarmDanger
        ),
        ManageModuleItem(
            route = "feed",
            title = "Thức ăn & Dinh dưỡng",
            subtitle = "Thư viện thức ăn, khẩu phần theo giai đoạn, kho & tiêu thụ",
            badge = "${feedInv.size} loại trong kho",
            icon = Icons.Default.Grass,
            accent = FarmSuccess
        ),
        ManageModuleItem(
            route = "tasks",
            title = "Lịch chăm sóc",
            subtitle = "Công việc hôm nay, quá hạn, lặp lại và ghi nhận kết quả",
            badge = "$pendingTasks việc cần làm",
            icon = Icons.Default.EventNote,
            accent = FarmWarning
        ),
        ManageModuleItem(
            route = "finance",
            title = "Tài chính thu chi",
            subtitle = "Ghi nhận chi phí, doanh thu, hóa đơn và biểu đồ theo tháng/chuồng",
            badge = "Lãi tạm: ${FarmFormatters.formatVnd(netProfit)}",
            icon = Icons.Default.AccountBalanceWallet,
            accent = MaterialTheme.colorScheme.primary
        )
    )

    Scaffold(
        topBar = { FarmTopBar(title = "Quản lý trang trại") }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = activeFarm?.name ?: "Trang trại hiện tại",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(modules, key = { it.route }) { item ->
                FarmCard(
                    onClick = { onNavigateRoute(item.route) },
                    modifier = Modifier.testTag("manage_module_${item.route}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(item.accent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.accent, modifier = Modifier.size(26.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                item.badge,
                                style = MaterialTheme.typography.labelMedium,
                                color = item.accent
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
