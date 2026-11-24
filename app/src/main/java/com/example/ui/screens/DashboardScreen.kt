package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FarmEntity
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmDanger
import com.example.ui.theme.FarmWarning
import com.example.ui.viewmodel.FarmViewModel
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: FarmViewModel,
    onNavigateRoute: (String) -> Unit,
    onOpenAnimal: (String) -> Unit,
    onOpenArticle: (String) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val farms by viewModel.farms.collectAsState()
    val activeFarm by viewModel.activeFarm.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val barns by viewModel.barns.collectAsState()
    val tasks by viewModel.careTasks.collectAsState()
    val events by viewModel.recentEvents.collectAsState()
    val articles by viewModel.publishedArticles.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Hôm nay") }
    var quickSearch by remember { mutableStateOf("") }
    var showFarmPicker by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val endOfPeriod = remember(selectedPeriod, now) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            "Hôm nay" -> {
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.timeInMillis
            }
            "Tuần này" -> now + 7 * 86_400_000L
            else -> now + 30 * 86_400_000L
        }
    }

    val pendingTasks = remember(tasks, endOfPeriod) {
        tasks.filter { !it.isCompleted && it.dueDateMillis <= endOfPeriod }
    }
    val upcomingTasks = remember(tasks, now) {
        tasks.filter { !it.isCompleted && it.dueDateMillis > now }.take(4)
    }
    val attentionAnimals = remember(animals) {
        animals.filter {
            it.status == "Cần theo dõi" || it.status == "Đang điều trị" || it.status == "Đang cách ly" || it.status == "Đang mang thai"
        }
    }
    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }

    val preferredSpecies = remember(user) {
        user?.preferredSpeciesCsv?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
    }
    val featuredArticles = remember(articles, preferredSpecies, quickSearch) {
        val base = if (quickSearch.isNotBlank()) {
            articles.filter {
                it.title.contains(quickSearch, ignoreCase = true) ||
                    it.species.contains(quickSearch, ignoreCase = true) ||
                    it.category.contains(quickSearch, ignoreCase = true)
            }
        } else {
            val prioritized = articles.filter { it.species in preferredSpecies || it.species == "Chung" }
            prioritized.ifEmpty { articles }
        }
        base.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting & Farm Selector Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = stringResource(R.string.cd_hero_banner),
                    modifier = Modifier
                        .matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xCC1B5E20),
                                    Color(0xDD144718)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Xin chào, ${user?.fullName ?: "Nhà nông"}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                onClick = { showFarmPicker = true },
                                shape = MaterialTheme.shapes.small,
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.testTag("farm_selector_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Agriculture, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = activeFarm?.name ?: "Chưa có trang trại",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge { Text(unreadNotifCount.toString()) }
                                }
                            }
                        ) {
                            FilledTonalIconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("notifications_button")
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = stringResource(R.string.cd_notifications)
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.app_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Period Filter & Quick Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChipRow(
                    options = listOf("Hôm nay", "Tuần này", "Tháng này"),
                    selected = selectedPeriod,
                    onSelected = { selectedPeriod = it }
                )
                FarmSearchField(
                    query = quickSearch,
                    onQueryChange = { quickSearch = it },
                    placeholder = "Tra cứu nhanh bệnh, kỹ thuật, vật nuôi..."
                )
            }
        }

        // Key Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricTile(
                        label = "Tổng vật nuôi",
                        value = animals.size.toString(),
                        icon = Icons.Default.Pets,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("animals") }
                    )
                    StatMetricTile(
                        label = "Chuồng trại",
                        value = barns.size.toString(),
                        icon = Icons.Default.Warehouse,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("barns") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricTile(
                        label = "Việc $selectedPeriod",
                        value = pendingTasks.size.toString(),
                        icon = Icons.Default.EventNote,
                        tint = FarmWarning,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("tasks") }
                    )
                    StatMetricTile(
                        label = "Cần theo dõi",
                        value = attentionAnimals.size.toString(),
                        icon = Icons.Default.MonitorHeart,
                        tint = FarmDanger,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateRoute("health") }
                    )
                }
            }
        }

        // Quick Actions (6 actions)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Thao tác nhanh", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile("Thêm vật nuôi", Icons.Default.AddCircle, Modifier.weight(1f)) { onNavigateRoute("animals") }
                    QuickActionTile("Tạo lịch", Icons.Default.CalendarMonth, Modifier.weight(1f)) { onNavigateRoute("tasks") }
                    QuickActionTile("Sức khỏe", Icons.Default.MedicalServices, Modifier.weight(1f)) { onNavigateRoute("health") }
                    QuickActionTile("Thu / Chi", Icons.Default.Payments, Modifier.weight(1f)) { onNavigateRoute("finance") }
                    QuickActionTile("Tra cứu bệnh", Icons.Default.MenuBook, Modifier.weight(1f)) { onNavigateRoute("knowledge") }
                    QuickActionTile("Hỏi AI", Icons.Default.SmartToy, Modifier.weight(1f)) { onNavigateRoute("assistant") }
                }
            }
        }

        // Animals Needing Attention
        if (attentionAnimals.isNotEmpty()) {
            item {
                SectionHeader("Vật nuôi cần theo dõi", "Xem tất cả") { onNavigateRoute("animals") }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(attentionAnimals, key = { it.id }) { animal ->
                        FarmCard(
                            modifier = Modifier.width(230.dp),
                            onClick = { onOpenAnimal(animal.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(animal.code, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                StatusBadge(animal.status)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(animal.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${animal.species} • ${FarmFormatters.formatWeight(animal.weightKg)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Upcoming & Today Tasks
        item {
            SectionHeader("Lịch chăm sóc sắp tới", "Mở lịch") { onNavigateRoute("tasks") }
        }
        if (pendingTasks.isEmpty() && upcomingTasks.isEmpty()) {
            item {
                FarmCard {
                    Text(
                        "Không có công việc tồn đọng trong khoảng thời gian này.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val shownTasks = (pendingTasks + upcomingTasks).distinctBy { it.id }.take(4)
            items(shownTasks, key = { it.id }) { task ->
                val isOverdue = !task.isCompleted && task.dueDateMillis < now
                FarmCard(onClick = { onNavigateRoute("tasks") }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { checked -> viewModel.toggleTaskCompleted(task, checked) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${task.taskType} • ${FarmFormatters.formatDateTime(task.dueDateMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue) FarmDanger else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(if (isOverdue) "Quá hạn" else task.priority)
                    }
                }
            }
        }

        // Recent Farm Activities
        if (events.isNotEmpty()) {
            item {
                SectionHeader("Hoạt động gần đây", null) {}
            }
            items(events.take(4), key = { it.id }) { ev ->
                FarmCard(onClick = { onOpenAnimal(ev.animalId) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ev.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${ev.eventType} • ${FarmFormatters.formatDate(ev.eventDateMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Featured Knowledge Articles
        item {
            SectionHeader("Kiến thức nổi bật", "Thư viện") { onNavigateRoute("knowledge") }
        }
        items(featuredArticles, key = { it.id }) { article ->
            FarmCard(onClick = { onOpenArticle(article.id) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(article.species)
                    Text(
                        "${article.readMinutes} phút đọc",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(article.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    if (showFarmPicker) {
        FarmSelectorDialog(
            farms = farms,
            activeFarmId = activeFarm?.id,
            onSelect = {
                viewModel.selectActiveFarm(it.id)
                showFarmPicker = false
            },
            onCreateNew = {
                showFarmPicker = false
                onNavigateRoute("account")
            },
            onDismiss = { showFarmPicker = false }
        )
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 78.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (actionText != null) {
            TextButton(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun FarmSelectorDialog(
    farms: List<FarmEntity>,
    activeFarmId: String?,
    onSelect: (FarmEntity) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn trang trại") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                farms.forEach { farm ->
                    val selected = farm.id == activeFarmId
                    Surface(
                        onClick = { onSelect(farm) },
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(farm.name, style = MaterialTheme.typography.titleSmall)
                                if (farm.address.isNotBlank()) {
                                    Text(farm.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNew) {
                Text("Quản lý trang trại")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}
