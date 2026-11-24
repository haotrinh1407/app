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
import androidx.compose.ui.unit.dp
import com.example.data.local.AnimalEntity
import com.example.data.local.BirthRecordEntity
import com.example.data.local.BreedingRecordEntity
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmDanger
import com.example.ui.theme.FarmSuccess
import com.example.ui.theme.FarmWarning
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun ReproductionScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val breedingList by viewModel.breedingRecords.collectAsState()
    val birthList by viewModel.birthRecords.collectAsState()

    var showAddBreeding by remember { mutableStateOf(false) }
    var editingBreeding by remember { mutableStateOf<BreedingRecordEntity?>(null) }
    var birthTarget by remember { mutableStateOf<BreedingRecordEntity?>(null) }
    var deletingBreeding by remember { mutableStateOf<BreedingRecordEntity?>(null) }

    val animalMap = remember(animals) { animals.associateBy { it.id } }
    val now = System.currentTimeMillis()
    val pregnantList = breedingList.filter { it.status == "Đang mang thai" }
    val upcomingBirths = pregnantList.filter {
        val exp = it.expectedBirthDateMillis ?: Long.MAX_VALUE
        exp in now..(now + 30L * 86_400_000L)
    }

    Scaffold(
        topBar = { FarmTopBar("Quản lý sinh sản", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingBreeding = null; showAddBreeding = true },
                modifier = Modifier.testTag("add_breeding_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ghi nhận phối giống")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 84.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMetricTile("Đang mang thai", pregnantList.size.toString(), Icons.Default.Favorite, FarmWarning, Modifier.weight(1f))
                    StatMetricTile("Sắp sinh (30 ngày)", upcomingBirths.size.toString(), Icons.Default.ChildFriendly, FarmDanger, Modifier.weight(1f))
                }
            }
            item {
                StatMetricTile("Đã sinh ghi nhận", birthList.size.toString(), Icons.Default.Celebration, FarmSuccess)
            }

            item {
                Text("Theo dõi phối giống & thai kỳ", style = MaterialTheme.typography.titleMedium)
            }

            if (breedingList.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Chưa có hồ sơ phối giống",
                        subtitle = "Ghi nhận ngày phối giống để FarmCare tự tính ngày sinh dự kiến và nhắc lịch chăm sóc.",
                        actionLabel = "Ghi nhận phối giống",
                        onAction = { editingBreeding = null; showAddBreeding = true },
                        showIllustration = false
                    )
                }
            } else {
                items(breedingList, key = { it.id }) { rec ->
                    val mother = animalMap[rec.motherId]
                    val father = animalMap[rec.fatherId]
                    FarmCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mother?.let { "${it.code} - ${it.name}" } ?: "Mẹ: ${rec.motherId}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(rec.status)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Bố: ${father?.let { "${it.code} - ${it.name}" } ?: "Không ghi nhận"} • ${rec.method}", style = MaterialTheme.typography.bodySmall)
                        Text("Ngày phối: ${FarmFormatters.formatDate(rec.breedingDateMillis)} • Thai kỳ tham khảo: ${rec.gestationDays} ngày", style = MaterialTheme.typography.bodySmall)
                        rec.expectedBirthDateMillis?.let {
                            Text("Ngày sinh dự kiến: ${FarmFormatters.formatDate(it)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        if (rec.stage.isNotBlank()) {
                            Text("Giai đoạn: ${rec.stage}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (rec.status != "Đã sinh") {
                                TextButton(onClick = { birthTarget = rec }) {
                                    Text("Ghi nhận sinh")
                                }
                            }
                            TextButton(onClick = { editingBreeding = rec; showAddBreeding = true }) { Text("Sửa") }
                            TextButton(onClick = { deletingBreeding = rec }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }

            if (birthList.isNotEmpty()) {
                item { Text("Lịch sử sinh con", style = MaterialTheme.typography.titleMedium) }
                items(birthList, key = { it.id }) { birth ->
                    val mother = animalMap[birth.motherId]
                    FarmCard {
                        Text("Mẹ: ${mother?.name ?: birth.motherId} — Sinh ngày ${FarmFormatters.formatDate(birth.actualBirthDateMillis)}", style = MaterialTheme.typography.titleSmall)
                        Text("Tổng sinh: ${birth.totalBorn} • Sống: ${birth.liveCount} • Chết: ${birth.deadCount} • TB: ${birth.avgOffspringWeightKg} kg", style = MaterialTheme.typography.bodySmall)
                        Text("Mẹ: ${birth.motherCondition} • Con: ${birth.offspringCondition}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAddBreeding) {
        BreedingFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            animals = animals,
            initial = editingBreeding,
            onSave = { rec ->
                viewModel.saveBreeding(rec)
                showAddBreeding = false
            },
            onDismiss = { showAddBreeding = false }
        )
    }

    birthTarget?.let { target ->
        BirthRecordDialog(
            breeding = target,
            mother = animalMap[target.motherId],
            onSave = { birth, createProfiles, gender ->
                viewModel.recordBirth(birth, target, createProfiles, gender)
                birthTarget = null
            },
            onDismiss = { birthTarget = null }
        )
    }

    deletingBreeding?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa hồ sơ phối giống",
            message = "Bạn có chắc muốn xóa bản ghi phối giống này?",
            onConfirm = {
                viewModel.deleteBreeding(target.id)
                deletingBreeding = null
            },
            onDismiss = { deletingBreeding = null }
        )
    }
}

@Composable
private fun BreedingFormDialog(
    farmId: String,
    animals: List<AnimalEntity>,
    initial: BreedingRecordEntity?,
    onSave: (BreedingRecordEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val femaleAnimals = animals.filter { it.gender != "Đực" }.ifEmpty { animals }
    val maleAnimals = animals.filter { it.gender == "Đực" }

    var motherId by remember { mutableStateOf(initial?.motherId ?: femaleAnimals.firstOrNull()?.id.orEmpty()) }
    var fatherId by remember { mutableStateOf(initial?.fatherId) }
    var method by remember { mutableStateOf(initial?.method ?: "Tự nhiên") }
    var performer by remember { mutableStateOf(initial?.performer ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: "Đang theo dõi") }
    var isPregnant by remember { mutableStateOf(initial?.isPregnant ?: false) }
    var stage by remember { mutableStateOf(initial?.stage ?: "Theo dõi sau phối") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    val motherSpecies = animals.firstOrNull { it.id == motherId }?.species ?: "Dê"
    var gestationDaysText by remember {
        mutableStateOf((initial?.gestationDays ?: FarmConstants.gestationDaysForSpecies(motherSpecies)).toString())
    }
    var offsetDaysFromTodayText by remember {
        val exp = initial?.expectedBirthDateMillis
        val diffDays = if (exp != null) ((exp - System.currentTimeMillis()) / 86_400_000L).toString()
        else FarmConstants.gestationDaysForSpecies(motherSpecies).toString()
        mutableStateOf(diffDays)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Ghi nhận phối giống" else "Cập nhật thai kỳ") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (femaleAnimals.isEmpty()) {
                    Text("Hãy thêm ít nhất một vật nuôi cái trước khi ghi nhận phối giống.", color = MaterialTheme.colorScheme.error)
                } else {
                    val mNames = femaleAnimals.map { "${it.code} - ${it.name}" }
                    val mSel = femaleAnimals.firstOrNull { it.id == motherId }?.let { "${it.code} - ${it.name}" } ?: mNames.first()
                    SimpleDropdownSelector("Vật nuôi mẹ *", mNames, mSel, { chosen ->
                        val picked = femaleAnimals.firstOrNull { "${it.code} - ${it.name}" == chosen }
                        if (picked != null) {
                            motherId = picked.id
                            val g = FarmConstants.gestationDaysForSpecies(picked.species)
                            gestationDaysText = g.toString()
                            if (initial == null) offsetDaysFromTodayText = g.toString()
                        }
                    })

                    val fNames = listOf("Không ghi nhận") + maleAnimals.map { "${it.code} - ${it.name}" }
                    val fSel = maleAnimals.firstOrNull { it.id == fatherId }?.let { "${it.code} - ${it.name}" } ?: "Không ghi nhận"
                    SimpleDropdownSelector("Vật nuôi bố", fNames, fSel, { chosen ->
                        fatherId = maleAnimals.firstOrNull { "${it.code} - ${it.name}" == chosen }?.id
                    })

                    SimpleDropdownSelector("Phương thức phối", listOf("Tự nhiên", "Thụ tinh nhân tạo"), method) { method = it }
                    SimpleDropdownSelector("Trạng thái", listOf("Đang theo dõi", "Đang mang thai", "Không đậu", "Đã sinh"), status) {
                        status = it
                        isPregnant = it == "Đang mang thai"
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gestationDaysText,
                            onValueChange = { gestationDaysText = it },
                            label = { Text("Thai kỳ (ngày)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = offsetDaysFromTodayText,
                            onValueChange = { offsetDaysFromTodayText = it },
                            label = { Text("Còn (ngày) sinh") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(value = stage, onValueChange = { stage = it }, label = { Text("Giai đoạn mang thai") }, singleLine = true)
                    OutlinedTextField(value = performer, onValueChange = { performer = it }, label = { Text("Người thực hiện") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") }, minLines = 2)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = femaleAnimals.isNotEmpty(),
                onClick = {
                    val now = System.currentTimeMillis()
                    val gest = gestationDaysText.toIntOrNull() ?: 150
                    val remDays = offsetDaysFromTodayText.toLongOrNull() ?: gest.toLong()
                    val expectedBirth = now + remDays * 86_400_000L
                    val entity = (initial ?: BreedingRecordEntity(
                        id = "",
                        farmId = farmId,
                        motherId = motherId,
                        breedingDateMillis = now
                    )).copy(
                        motherId = motherId,
                        fatherId = fatherId,
                        method = method,
                        performer = performer.trim(),
                        isPregnant = isPregnant || status == "Đang mang thai",
                        confirmedDateMillis = if (status == "Đang mang thai") (initial?.confirmedDateMillis ?: now) else initial?.confirmedDateMillis,
                        gestationDays = gest,
                        expectedBirthDateMillis = expectedBirth,
                        stage = stage.trim(),
                        status = status,
                        notes = notes.trim()
                    )
                    onSave(entity)
                }
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun BirthRecordDialog(
    breeding: BreedingRecordEntity,
    mother: AnimalEntity?,
    onSave: (BirthRecordEntity, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var totalBornText by remember { mutableStateOf("1") }
    var liveCountText by remember { mutableStateOf("1") }
    var avgWeightText by remember { mutableStateOf("2.5") }
    var offspringCodes by remember { mutableStateOf("${mother?.code ?: "CON"}-C1") }
    var motherCondition by remember { mutableStateOf("Ổn định") }
    var offspringCondition by remember { mutableStateOf("Khỏe mạnh") }
    var createProfiles by remember { mutableStateOf(true) }
    var offspringGender by remember { mutableStateOf("Cái") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi nhận ca sinh (${mother?.name ?: ""})") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalBornText,
                        onValueChange = { totalBornText = it },
                        label = { Text("Tổng con sinh") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = liveCountText,
                        onValueChange = { liveCountText = it },
                        label = { Text("Số con sống") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = avgWeightText,
                    onValueChange = { avgWeightText = it },
                    label = { Text("Trọng lượng TB con non (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = offspringCodes,
                    onValueChange = { offspringCodes = it },
                    label = { Text("Mã con non (cách nhau dấu phẩy)") },
                    singleLine = true
                )
                OutlinedTextField(value = motherCondition, onValueChange = { motherCondition = it }, label = { Text("Tình trạng mẹ") }, singleLine = true)
                OutlinedTextField(value = offspringCondition, onValueChange = { offspringCondition = it }, label = { Text("Tình trạng con") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = createProfiles, onCheckedChange = { createProfiles = it })
                    Text("Tự động tạo hồ sơ con non liên kết mẹ/bố", style = MaterialTheme.typography.bodySmall)
                }
                if (createProfiles) {
                    SimpleDropdownSelector("Giới tính mặc định con non", FarmConstants.GENDER_LIST, offspringGender) { offspringGender = it }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = (totalBornText.toIntOrNull() ?: 1).coerceAtLeast(1)
                val live = (liveCountText.toIntOrNull() ?: total).coerceIn(0, total)
                val birth = BirthRecordEntity(
                    id = "",
                    farmId = breeding.farmId,
                    breedingId = breeding.id,
                    motherId = breeding.motherId,
                    actualBirthDateMillis = System.currentTimeMillis(),
                    totalBorn = total,
                    liveCount = live,
                    deadCount = (total - live).coerceAtLeast(0),
                    avgOffspringWeightKg = avgWeightText.toDoubleOrNull() ?: 0.0,
                    offspringCodes = offspringCodes.trim(),
                    motherCondition = motherCondition.trim(),
                    offspringCondition = offspringCondition.trim(),
                    notes = notes.trim()
                )
                onSave(birth, createProfiles, offspringGender)
            }) { Text("Xác nhận sinh") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
