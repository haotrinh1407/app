package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.AnimalEntity
import com.example.data.local.BarnEntity
import com.example.data.local.CareTaskEntity
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmDanger
import com.example.ui.theme.FarmSuccess
import com.example.ui.theme.FarmWarning
import com.example.ui.viewmodel.FarmViewModel
import java.util.Calendar

@Composable
fun TasksScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val tasks by viewModel.careTasks.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val barns by viewModel.barns.collectAsState()

    var viewRange by remember { mutableStateOf("Tất cả") } // Ngày | Tuần | Tháng | Tất cả
    var statusTab by remember { mutableStateOf("Cần làm") } // Cần làm | Quá hạn | Đã xong
    var editingTask by remember { mutableStateOf<CareTaskEntity?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var completingTask by remember { mutableStateOf<CareTaskEntity?>(null) }
    var deletingTask by remember { mutableStateOf<CareTaskEntity?>(null) }

    val now = System.currentTimeMillis()
    val endOfToday = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.timeInMillis
    }

    val todayCount = tasks.count { !it.isCompleted && it.dueDateMillis in now..endOfToday }
    val overdueCount = tasks.count { !it.isCompleted && it.dueDateMillis < now }
    val upcomingCount = tasks.count { !it.isCompleted && it.dueDateMillis > endOfToday }
    val completedCount = tasks.count { it.isCompleted }

    val filtered = remember(tasks, viewRange, statusTab, now, endOfToday) {
        tasks.filter { task ->
            val matchRange = when (viewRange) {
                "Ngày" -> task.dueDateMillis <= endOfToday
                "Tuần" -> task.dueDateMillis <= now + 7L * 86_400_000L
                "Tháng" -> task.dueDateMillis <= now + 30L * 86_400_000L
                else -> true
            }
            val matchStatus = when (statusTab) {
                "Quá hạn" -> !task.isCompleted && task.dueDateMillis < now
                "Đã xong" -> task.isCompleted
                else -> !task.isCompleted
            }
            matchRange && matchStatus
        }
    }

    val animalMap = remember(animals) { animals.associateBy { it.id } }
    val barnMap = remember(barns) { barns.associateBy { it.id } }

    Scaffold(
        topBar = { FarmTopBar("Lịch chăm sóc", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingTask = null; showTaskDialog = true },
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo lịch")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatMetricTile("Hôm nay", todayCount.toString(), Icons.Default.Today, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatMetricTile("Quá hạn", overdueCount.toString(), Icons.Default.Warning, FarmDanger, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatMetricTile("Sắp tới", upcomingCount.toString(), Icons.Default.Schedule, FarmWarning, Modifier.weight(1f))
                StatMetricTile("Hoàn thành", completedCount.toString(), Icons.Default.TaskAlt, FarmSuccess, Modifier.weight(1f))
            }

            FilterChipRow(
                options = listOf("Tất cả", "Ngày", "Tuần", "Tháng"),
                selected = viewRange,
                onSelected = { viewRange = it }
            )
            FilterChipRow(
                options = listOf("Cần làm", "Quá hạn", "Đã xong"),
                selected = statusTab,
                onSelected = { statusTab = it }
            )

            if (filtered.isEmpty()) {
                EmptyStateView(
                    title = "Không có công việc nào",
                    subtitle = "Tạo lịch cho ăn, vệ sinh, tiêm phòng hoặc theo dõi sinh sản.",
                    actionLabel = "Tạo lịch chăm sóc",
                    onAction = { editingTask = null; showTaskDialog = true },
                    showIllustration = false
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { task ->
                        val isOverdue = !task.isCompleted && task.dueDateMillis < now
                        FarmCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { checked ->
                                        if (checked) completingTask = task
                                        else viewModel.toggleTaskCompleted(task, false)
                                    }
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                                    )
                                    Text(
                                        text = "${task.taskType} • ${FarmFormatters.formatDateTime(task.dueDateMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOverdue) FarmDanger else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val targetText = listOfNotNull(
                                        animalMap[task.animalId]?.name,
                                        barnMap[task.barnId]?.name,
                                        task.assignee.takeIf { it.isNotBlank() }?.let { "Phụ trách: $it" }
                                    ).joinToString(" • ")
                                    if (targetText.isNotBlank()) {
                                        Text(targetText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (task.repeatRule != "Không lặp") {
                                        Text("Lặp: ${task.repeatRule}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (task.resultNotes.isNotBlank()) {
                                        Text("Kết quả: ${task.resultNotes}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                StatusBadge(if (isOverdue) "Quá hạn" else if (task.isCompleted) "Hoàn thành" else task.priority)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { editingTask = task; showTaskDialog = true }) { Text("Sửa") }
                                TextButton(onClick = { deletingTask = task }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        CareTaskFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            animals = animals,
            barns = barns,
            initial = editingTask,
            onSave = { viewModel.saveCareTask(it); showTaskDialog = false },
            onDismiss = { showTaskDialog = false }
        )
    }

    completingTask?.let { task ->
        var resultNotes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { completingTask = null },
            title = { Text("Hoàn thành công việc") },
            text = {
                OutlinedTextField(
                    value = resultNotes,
                    onValueChange = { resultNotes = it },
                    label = { Text("Ghi chú kết quả (tùy chọn)") },
                    minLines = 2
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.toggleTaskCompleted(task, true, resultNotes.trim())
                    completingTask = null
                }) { Text("Xác nhận xong") }
            },
            dismissButton = { TextButton(onClick = { completingTask = null }) { Text("Hủy") } }
        )
    }

    deletingTask?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa lịch chăm sóc",
            message = "Xóa công việc \"${target.title}\"?",
            onConfirm = { viewModel.deleteCareTask(target.id); deletingTask = null },
            onDismiss = { deletingTask = null }
        )
    }
}

@Composable
private fun CareTaskFormDialog(
    farmId: String,
    animals: List<AnimalEntity>,
    barns: List<BarnEntity>,
    initial: CareTaskEntity?,
    onSave: (CareTaskEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var taskType by remember { mutableStateOf(initial?.taskType ?: FarmConstants.TASK_TYPES.first()) }
    var priority by remember { mutableStateOf(initial?.priority ?: "Trung bình") }
    var repeatRule by remember { mutableStateOf(initial?.repeatRule ?: "Không lặp") }
    var hoursFromNow by remember { mutableStateOf("4") }
    var assignee by remember { mutableStateOf(initial?.assignee ?: "") }
    var selectedAnimalId by remember { mutableStateOf(initial?.animalId) }
    var selectedBarnId by remember { mutableStateOf(initial?.barnId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tạo lịch chăm sóc" else "Sửa lịch chăm sóc") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên công việc *") }, singleLine = true)
                SimpleDropdownSelector("Loại lịch", FarmConstants.TASK_TYPES, taskType) { taskType = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimpleDropdownSelector("Mức ưu tiên", FarmConstants.TASK_PRIORITIES, priority, { priority = it }, Modifier.weight(1f))
                    SimpleDropdownSelector("Lặp lại", FarmConstants.REPEAT_RULES, repeatRule, { repeatRule = it }, Modifier.weight(1f))
                }
                if (initial == null) {
                    OutlinedTextField(
                        value = hoursFromNow,
                        onValueChange = { hoursFromNow = it },
                        label = { Text("Thực hiện sau (giờ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                if (animals.isNotEmpty()) {
                    val aLabels = listOf("Không chọn") + animals.map { "${it.code} - ${it.name}" }
                    val aSel = animals.firstOrNull { it.id == selectedAnimalId }?.let { "${it.code} - ${it.name}" } ?: "Không chọn"
                    SimpleDropdownSelector("Gán vật nuôi", aLabels, aSel) { chosen ->
                        selectedAnimalId = animals.firstOrNull { "${it.code} - ${it.name}" == chosen }?.id
                    }
                }
                if (barns.isNotEmpty()) {
                    val bLabels = listOf("Không chọn") + barns.map { it.name }
                    val bSel = barns.firstOrNull { it.id == selectedBarnId }?.name ?: "Không chọn"
                    SimpleDropdownSelector("Gán chuồng", bLabels, bSel) { chosen ->
                        selectedBarnId = barns.firstOrNull { it.name == chosen }?.id
                    }
                }
                OutlinedTextField(value = assignee, onValueChange = { assignee = it }, label = { Text("Người phụ trách") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(enabled = title.isNotBlank(), onClick = {
                val due = initial?.dueDateMillis ?: (System.currentTimeMillis() + (hoursFromNow.toLongOrNull() ?: 4L) * 3_600_000L)
                val entity = (initial ?: CareTaskEntity(
                    id = "",
                    farmId = farmId,
                    title = title.trim(),
                    taskType = taskType,
                    dueDateMillis = due
                )).copy(
                    title = title.trim(),
                    taskType = taskType,
                    priority = priority,
                    repeatRule = repeatRule,
                    animalId = selectedAnimalId,
                    barnId = selectedBarnId,
                    assignee = assignee.trim()
                )
                onSave(entity)
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
