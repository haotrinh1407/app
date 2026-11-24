package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.BarnEntity
import com.example.data.local.FinancialTransactionEntity
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun BarnsScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit,
    onOpenAnimal: (String) -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val barns by viewModel.barns.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var editingBarn by remember { mutableStateOf<BarnEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var deletingBarn by remember { mutableStateOf<BarnEntity?>(null) }
    var inspectingBarn by remember { mutableStateOf<BarnEntity?>(null) }
    var barnExpenseTarget by remember { mutableStateOf<BarnEntity?>(null) }

    Scaffold(
        topBar = { FarmTopBar(title = "Quản lý chuồng trại (${barns.size})", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingBarn = null; showFormDialog = true },
                modifier = Modifier.testTag("add_barn_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm chuồng")
            }
        }
    ) { innerPadding ->
        if (barns.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    title = "Chưa có chuồng trại nào",
                    subtitle = "Thêm thông tin chuồng nuôi để quản lý sức chứa, vệ sinh và phân bổ đàn.",
                    actionLabel = "Thêm chuồng mới",
                    onAction = { editingBarn = null; showFormDialog = true }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 84.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(barns, key = { it.id }) { barn ->
                    val insideAnimals = animals.filter { it.barnId == barn.id }
                    val barnExpenses = transactions
                        .filter { it.barnId == barn.id && it.txType == "EXPENSE" }
                        .sumOf { it.amount }
                    val occupancyRatio = if (barn.capacity > 0) (insideAnimals.size.toFloat() / barn.capacity).coerceIn(0f, 1f) else 0f

                    FarmCard(onClick = { inspectingBarn = barn }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!barn.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = barn.photoUri,
                                    contentDescription = barn.name,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warehouse, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${barn.code} • ${barn.species}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    StatusBadge(barn.status)
                                }
                                Text(barn.name, style = MaterialTheme.typography.titleMedium)
                                Text("${barn.barnType} • ${barn.location.ifBlank { "Chưa rõ vị trí" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Số lượng hiện tại: ${insideAnimals.size} / ${barn.capacity}", style = MaterialTheme.typography.bodySmall)
                            if (barnExpenses > 0) {
                                Text("Chi phí: ${FarmFormatters.formatVnd(barnExpenses)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { occupancyRatio },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Vệ sinh: ${FarmFormatters.formatDate(barn.lastCleanedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Sửa chữa: ${FarmFormatters.formatDate(barn.lastRepairedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.markBarnCleaned(barn) },
                                modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Vệ sinh", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { viewModel.markBarnRepaired(barn) },
                                modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Sửa chữa", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { barnExpenseTarget = barn },
                                modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Chi phí", maxLines = 1)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { editingBarn = barn; showFormDialog = true }) { Text("Sửa") }
                            TextButton(onClick = { deletingBarn = barn }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }

    inspectingBarn?.let { barn ->
        val inside = animals.filter { it.barnId == barn.id }
        AlertDialog(
            onDismissRequest = { inspectingBarn = null },
            title = { Text("${barn.name} (${inside.size} vật nuôi)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (inside.isEmpty()) {
                        Text("Hiện chưa có vật nuôi nào trong chuồng này.")
                    } else {
                        inside.forEach { an ->
                            Surface(
                                onClick = {
                                    inspectingBarn = null
                                    onOpenAnimal(an.id)
                                },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("${an.code} - ${an.name}", style = MaterialTheme.typography.titleSmall)
                                        Text("${an.species} • ${FarmFormatters.formatWeight(an.weightKg)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    StatusBadge(an.status)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectingBarn = null }) { Text("Đóng") }
            }
        )
    }

    barnExpenseTarget?.let { barn ->
        var amountText by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("Chi phí chuồng ${barn.name}") }
        AlertDialog(
            onDismissRequest = { barnExpenseTarget = null },
            title = { Text("Ghi nhận chi phí chuồng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Số tiền (đ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Nội dung chi phí") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0L) {
                        viewModel.saveTransaction(
                            FinancialTransactionEntity(
                                id = "",
                                farmId = activeFarm?.id.orEmpty(),
                                txType = "EXPENSE",
                                category = "Chuồng trại",
                                amount = amt,
                                txDateMillis = System.currentTimeMillis(),
                                description = desc.ifBlank { "Chi phí ${barn.name}" },
                                barnId = barn.id
                            )
                        )
                        barnExpenseTarget = null
                    }
                }) { Text("Lưu chi phí") }
            },
            dismissButton = { TextButton(onClick = { barnExpenseTarget = null }) { Text("Hủy") } }
        )
    }

    if (showFormDialog) {
        BarnFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            initial = editingBarn,
            onSave = { barn ->
                viewModel.saveBarn(barn)
                showFormDialog = false
            },
            onDismiss = { showFormDialog = false }
        )
    }

    deletingBarn?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa chuồng",
            message = "Bạn chắc chắn muốn xóa ${target.name}? Vật nuôi bên trong sẽ được giữ lại ở trạng thái chưa xếp chuồng.",
            onConfirm = {
                viewModel.deleteBarn(target.id)
                deletingBarn = null
            },
            onDismiss = { deletingBarn = null }
        )
    }
}

@Composable
private fun BarnFormDialog(
    farmId: String,
    initial: BarnEntity?,
    onSave: (BarnEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var barnType by remember { mutableStateOf(initial?.barnType ?: FarmConstants.BARN_TYPES.first()) }
    var species by remember { mutableStateOf(initial?.species ?: "Dê") }
    var capacityText by remember { mutableStateOf(initial?.capacity?.toString() ?: "20") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: FarmConstants.BARN_STATUSES.first()) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var photoUri by remember { mutableStateOf(initial?.photoUri) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Thêm chuồng mới" else "Sửa thông tin chuồng") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!error.isNullOrBlank()) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                PhotoPickerRow(photoUri = photoUri, onPhotoSelected = { photoUri = it }, label = "Ảnh chuồng")
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Mã chuồng *") }, singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên chuồng *") }, singleLine = true)
                SimpleDropdownSelector("Loại chuồng", FarmConstants.BARN_TYPES, barnType) { barnType = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimpleDropdownSelector("Loài", FarmConstants.SPECIES_LIST, species, { species = it }, Modifier.weight(1f))
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it },
                        label = { Text("Sức chứa") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                SimpleDropdownSelector("Trạng thái", FarmConstants.BARN_STATUSES, status) { status = it }
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Vị trí") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") }, minLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (code.isBlank() || name.isBlank()) {
                    error = "Vui lòng nhập mã chuồng và tên chuồng."
                    return@Button
                }
                val entity = (initial ?: BarnEntity(
                    id = "",
                    farmId = farmId,
                    code = code.trim(),
                    name = name.trim(),
                    barnType = barnType,
                    species = species,
                    capacity = capacityText.toIntOrNull() ?: 20
                )).copy(
                    code = code.trim(),
                    name = name.trim(),
                    barnType = barnType,
                    species = species,
                    capacity = capacityText.toIntOrNull() ?: 20,
                    location = location.trim(),
                    status = status,
                    notes = notes.trim(),
                    photoUri = photoUri
                )
                onSave(entity)
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
