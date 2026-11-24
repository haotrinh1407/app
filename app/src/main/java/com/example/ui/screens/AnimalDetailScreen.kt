package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmDanger
import com.example.ui.theme.FarmSuccess
import com.example.ui.viewmodel.FarmViewModel

private val DETAIL_TABS = listOf(
    "Tổng quan", "Sức khỏe", "Sinh sản", "Cân nặng", "Lịch chăm sóc", "Chi phí", "Lịch sử"
)

@Composable
fun AnimalDetailScreen(
    animalId: String,
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val animal by viewModel.observeAnimal(animalId).collectAsState(initial = null)
    val events by viewModel.observeAnimalEvents(animalId).collectAsState(initial = emptyList())
    val barns by viewModel.barns.collectAsState()
    val healthList by viewModel.healthRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val dewormings by viewModel.dewormings.collectAsState()
    val breedingList by viewModel.breedingRecords.collectAsState()
    val birthList by viewModel.birthRecords.collectAsState()
    val tasks by viewModel.careTasks.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    val current = animal
    if (current == null) {
        Scaffold(topBar = { FarmTopBar("Chi tiết vật nuôi", onBack = onBack) }) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy hồ sơ vật nuôi.")
            }
        }
        return
    }

    val barnName = barns.firstOrNull { it.id == current.barnId }?.name ?: "Chưa xếp chuồng"
    val animalHealth = healthList.filter { it.animalId == animalId }
    val animalVaccines = vaccinations.filter { it.animalId == animalId }
    val animalDeworms = dewormings.filter { it.animalId == animalId }
    val animalBreeding = breedingList.filter { it.motherId == animalId || it.fatherId == animalId }
    val animalBirths = birthList.filter { it.motherId == animalId }
    val animalTasks = tasks.filter { it.animalId == animalId }
    val animalTx = transactions.filter { it.animalId == animalId }
    val weightEvents = events.filter { it.weightSnapshotKg != null || it.eventType == "Cân" }

    Scaffold(
        topBar = { FarmTopBar(title = "${current.code} • ${current.name}", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                DETAIL_TABS.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Tổng quan
                        item {
                            FarmCard {
                                if (!current.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = current.photoUri,
                                        contentDescription = current.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(MaterialTheme.shapes.medium),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(current.name, style = MaterialTheme.typography.titleLarge)
                                    StatusBadge(current.status)
                                }
                                Spacer(Modifier.height(10.dp))
                                InfoRow("Mã vật nuôi", current.code)
                                InfoRow("Loài / Giống", "${current.species} • ${current.breed.ifBlank { "Chưa rõ" }}")
                                InfoRow("Giới tính", current.gender)
                                InfoRow("Cân nặng hiện tại", FarmFormatters.formatWeight(current.weightKg))
                                InfoRow("Chuồng hiện tại", barnName)
                                InfoRow("Ngày nhập", FarmFormatters.formatDate(current.intakeDateMillis))
                                InfoRow("Nguồn gốc", current.origin.ifBlank { "Chưa ghi nhận" })
                                InfoRow("Giá mua", if (current.purchasePrice > 0) FarmFormatters.formatVnd(current.purchasePrice) else "Chưa ghi nhận")
                                InfoRow("Người phụ trách", current.caretaker.ifBlank { "Chưa phân công" })
                                if (current.notes.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Ghi chú: ${current.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showWeightDialog = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                                    Text("Cân mới")
                                }
                                OutlinedButton(onClick = { showTransferDialog = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                                    Text("Chuyển chuồng")
                                }
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { showStatusDialog = true },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.Sync, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Cập nhật trạng thái (${current.status})")
                            }
                        }
                    }

                    1 -> { // Sức khỏe
                        item {
                            Text("Hồ sơ bệnh & điều trị (${animalHealth.size})", style = MaterialTheme.typography.titleMedium)
                        }
                        if (animalHealth.isEmpty() && animalVaccines.isEmpty() && animalDeworms.isEmpty()) {
                            item { EmptyStateView("Chưa có ghi nhận sức khỏe", "Vật nuôi chưa có lịch sử bệnh, tiêm phòng hay tẩy giun.", showIllustration = false) }
                        } else {
                            items(animalHealth, key = { it.id }) { hr ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(hr.symptoms, style = MaterialTheme.typography.titleSmall)
                                        StatusBadge(hr.status)
                                    }
                                    Text("Phát hiện: ${FarmFormatters.formatDate(hr.detectedDateMillis)} • Mức độ: ${hr.severity}", style = MaterialTheme.typography.bodySmall)
                                    if (hr.vetDiagnosis.isNotBlank()) Text("Chẩn đoán: ${hr.vetDiagnosis}", style = MaterialTheme.typography.bodySmall)
                                    if (hr.confirmedTreatment.isNotBlank()) Text("Điều trị: ${hr.confirmedTreatment}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            items(animalVaccines, key = { it.id }) { vac ->
                                FarmCard {
                                    Text("Tiêm vaccine: ${vac.vaccineName}", style = MaterialTheme.typography.titleSmall)
                                    Text("Ngày tiêm: ${FarmFormatters.formatDate(vac.vaccinatedDateMillis)}", style = MaterialTheme.typography.bodySmall)
                                    vac.nextFollowUpMillis?.let {
                                        Text("Nhắc lại: ${FarmFormatters.formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            items(animalDeworms, key = { it.id }) { dew ->
                                FarmCard {
                                    Text("Tẩy giun: ${dew.productUsed}", style = MaterialTheme.typography.titleSmall)
                                    Text("Ngày thực hiện: ${FarmFormatters.formatDate(dew.dewormDateMillis)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    2 -> { // Sinh sản
                        if (animalBreeding.isEmpty() && animalBirths.isEmpty()) {
                            item { EmptyStateView("Chưa có dữ liệu sinh sản", "Chưa có bản ghi phối giống hoặc sinh con liên quan.", showIllustration = false) }
                        } else {
                            items(animalBreeding, key = { it.id }) { br ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Phối giống (${br.method})", style = MaterialTheme.typography.titleSmall)
                                        StatusBadge(br.status)
                                    }
                                    Text("Ngày phối: ${FarmFormatters.formatDate(br.breedingDateMillis)}", style = MaterialTheme.typography.bodySmall)
                                    br.expectedBirthDateMillis?.let {
                                        Text("Dự kiến sinh: ${FarmFormatters.formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            items(animalBirths, key = { it.id }) { b ->
                                FarmCard {
                                    Text("Sinh ngày ${FarmFormatters.formatDate(b.actualBirthDateMillis)}", style = MaterialTheme.typography.titleSmall)
                                    Text("Tổng sinh: ${b.totalBorn} • Sống: ${b.liveCount} • Chết: ${b.deadCount}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    3 -> { // Cân nặng
                        item {
                            Button(onClick = { showWeightDialog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                Icon(Icons.Default.MonitorWeight, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Ghi nhận cân nặng mới")
                            }
                        }
                        if (weightEvents.isEmpty()) {
                            item { EmptyStateView("Chưa có lịch sử cân nặng", "Hãy cập nhật cân nặng định kỳ để theo dõi tăng trưởng.", showIllustration = false) }
                        } else {
                            items(weightEvents, key = { it.id }) { ev ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(ev.title, style = MaterialTheme.typography.titleSmall)
                                        Text(FarmFormatters.formatDate(ev.eventDateMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (ev.description.isNotBlank()) Text(ev.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    4 -> { // Lịch chăm sóc
                        if (animalTasks.isEmpty()) {
                            item { EmptyStateView("Chưa có lịch chăm sóc riêng", "Bạn có thể gán lịch chăm sóc cho vật nuôi này ở mục Lịch chăm sóc.", showIllustration = false) }
                        } else {
                            items(animalTasks, key = { it.id }) { task ->
                                FarmCard {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Checkbox(checked = task.isCompleted, onCheckedChange = { viewModel.toggleTaskCompleted(task, it) })
                                        Column(Modifier.weight(1f)) {
                                            Text(task.title, style = MaterialTheme.typography.titleSmall)
                                            Text("${task.taskType} • ${FarmFormatters.formatDateTime(task.dueDateMillis)}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        StatusBadge(if (task.isCompleted) "Hoàn thành" else task.priority)
                                    }
                                }
                            }
                        }
                    }

                    5 -> { // Chi phí
                        val totalExp = animalTx.filter { it.txType == "EXPENSE" }.sumOf { it.amount }
                        val totalInc = animalTx.filter { it.txType == "INCOME" }.sumOf { it.amount }
                        item {
                            FarmCard {
                                InfoRow("Tổng chi phí gắn với vật nuôi", FarmFormatters.formatVnd(totalExp))
                                InfoRow("Tổng doanh thu gắn với vật nuôi", FarmFormatters.formatVnd(totalInc))
                                InfoRow("Giá mua ban đầu", FarmFormatters.formatVnd(current.purchasePrice))
                            }
                        }
                        items(animalTx, key = { it.id }) { tx ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tx.description, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Text(
                                        text = (if (tx.txType == "INCOME") "+" else "-") + FarmFormatters.formatVnd(tx.amount),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (tx.txType == "INCOME") FarmSuccess else FarmDanger
                                    )
                                }
                                Text("${tx.category} • ${FarmFormatters.formatDate(tx.txDateMillis)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    6 -> { // Lịch sử
                        if (events.isEmpty()) {
                            item { EmptyStateView("Chưa có sự kiện nào", "Các sự kiện nhập đàn, chuyển chuồng, cân nặng, khám bệnh sẽ xuất hiện tại đây.", showIllustration = false) }
                        } else {
                            items(events, key = { it.id }) { ev ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        StatusBadge(ev.eventType)
                                        Text(FarmFormatters.formatDateTime(ev.eventDateMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(ev.title, style = MaterialTheme.typography.titleSmall)
                                    if (ev.description.isNotBlank()) {
                                        Text(ev.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWeightDialog) {
        var wText by remember { mutableStateOf(current.weightKg.toString()) }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Cập nhật cân nặng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = wText,
                        onValueChange = { wText = it },
                        label = { Text("Cân nặng mới (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Ghi chú (tùy chọn)") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val kg = wText.toDoubleOrNull()
                    if (kg != null && kg > 0) {
                        viewModel.recordWeight(current, kg, note)
                        showWeightDialog = false
                    }
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showWeightDialog = false }) { Text("Hủy") } }
        )
    }

    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Chuyển chuồng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (barns.isEmpty()) {
                        Text("Chưa có chuồng nào để chuyển.")
                    } else {
                        barns.forEach { barn ->
                            Surface(
                                onClick = {
                                    viewModel.transferAnimal(current, barn)
                                    showTransferDialog = false
                                },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${barn.name} (${barn.species})", style = MaterialTheme.typography.titleSmall)
                                    Text(barn.code, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTransferDialog = false }) { Text("Đóng") } }
        )
    }

    if (showStatusDialog) {
        var chosen by remember { mutableStateOf(current.status) }
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Đổi trạng thái vật nuôi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SimpleDropdownSelector(
                        label = "Trạng thái mới",
                        options = FarmConstants.ANIMAL_STATUSES,
                        selected = chosen,
                        onSelected = { chosen = it }
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Lý do / Ghi chú") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateAnimalStatus(current, chosen, reason)
                    showStatusDialog = false
                }) { Text("Cập nhật") }
            },
            dismissButton = { TextButton(onClick = { showStatusDialog = false }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
