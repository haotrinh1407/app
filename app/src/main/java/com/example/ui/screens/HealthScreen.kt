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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.*
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmOnWarningContainer
import com.example.ui.theme.FarmWarningContainer
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun HealthScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val healthRecords by viewModel.healthRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val dewormings by viewModel.dewormings.collectAsState()
    val medications by viewModel.medications.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddHealth by remember { mutableStateOf(false) }
    var showAddVaccine by remember { mutableStateOf(false) }
    var showAddDeworm by remember { mutableStateOf(false) }
    var showAddMed by remember { mutableStateOf(false) }
    var deletingHealth by remember { mutableStateOf<HealthRecordEntity?>(null) }

    val animalMap = remember(animals) { animals.associateBy { it.id } }

    Scaffold(
        topBar = { FarmTopBar("Sức khỏe & Thú y", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (selectedTab) {
                    0 -> showAddHealth = true
                    1 -> showAddVaccine = true
                    2 -> showAddDeworm = true
                    else -> showAddMed = true
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mới")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Bệnh án") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Tiêm phòng") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Tẩy giun") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Kho thuốc") })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 84.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = FarmWarningContainer
                    ) {
                        Text(
                            text = stringResource(R.string.vet_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmOnWarningContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                when (selectedTab) {
                    0 -> {
                        if (healthRecords.isEmpty()) {
                            item { EmptyStateView("Chưa có hồ sơ sức khỏe", "Nhấn nút + để ghi nhận triệu chứng và theo dõi điều trị.", showIllustration = false) }
                        } else {
                            items(healthRecords, key = { it.id }) { hr ->
                                val animal = animalMap[hr.animalId]
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(animal?.let { "${it.code} - ${it.name}" } ?: hr.animalId, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        StatusBadge(hr.status)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Triệu chứng: ${hr.symptoms} (Mức độ: ${hr.severity})", style = MaterialTheme.typography.bodyMedium)
                                    Text("Phát hiện: ${FarmFormatters.formatDate(hr.detectedDateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (hr.vetDiagnosis.isNotBlank()) Text("Chẩn đoán thú y: ${hr.vetDiagnosis}", style = MaterialTheme.typography.bodySmall)
                                    if (hr.confirmedTreatment.isNotBlank()) Text("Điều trị xác nhận: ${hr.confirmedTreatment}", style = MaterialTheme.typography.bodySmall)
                                    if (!hr.photoUri.isNullOrBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        AsyncImage(
                                            model = hr.photoUri,
                                            contentDescription = hr.symptoms,
                                            modifier = Modifier.fillMaxWidth().height(120.dp).clip(MaterialTheme.shapes.small),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        if (hr.status != "Đã hồi phục") {
                                            TextButton(onClick = {
                                                viewModel.saveHealth(hr.copy(status = "Đã hồi phục"))
                                            }) { Text("Đánh dấu hồi phục") }
                                        }
                                        TextButton(onClick = { deletingHealth = hr }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        if (vaccinations.isEmpty()) {
                            item { EmptyStateView("Chưa có lịch tiêm phòng", "Ghi nhận mũi tiêm vaccine để không bỏ lỡ lịch nhắc lại.", showIllustration = false) }
                        } else {
                            items(vaccinations, key = { it.id }) { vac ->
                                val animal = animalMap[vac.animalId]
                                FarmCard {
                                    Text("${vac.vaccineName} — ${animal?.let { "${it.code} (${it.name})" } ?: vac.animalId}", style = MaterialTheme.typography.titleSmall)
                                    Text("Ngày tiêm: ${FarmFormatters.formatDate(vac.vaccinatedDateMillis)} • Người tiêm: ${vac.performer.ifBlank { "Chưa rõ" }}", style = MaterialTheme.typography.bodySmall)
                                    vac.nextFollowUpMillis?.let {
                                        Text("Ngày theo dõi/nhắc lại: ${FarmFormatters.formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { viewModel.deleteVaccination(vac.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        if (dewormings.isEmpty()) {
                            item { EmptyStateView("Chưa có lịch tẩy giun", "Ghi nhận sản phẩm tẩy giun và lịch định kỳ.", showIllustration = false) }
                        } else {
                            items(dewormings, key = { it.id }) { dew ->
                                val animal = animalMap[dew.animalId]
                                FarmCard {
                                    Text("${dew.productUsed} — ${animal?.let { "${it.code} (${it.name})" } ?: dew.animalId}", style = MaterialTheme.typography.titleSmall)
                                    Text("Ngày thực hiện: ${FarmFormatters.formatDate(dew.dewormDateMillis)}", style = MaterialTheme.typography.bodySmall)
                                    dew.nextScheduleMillis?.let {
                                        Text("Lịch tiếp theo: ${FarmFormatters.formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { viewModel.deleteDeworming(dew.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        if (medications.isEmpty()) {
                            item { EmptyStateView("Kho thuốc đang trống", "Theo dõi số lượng và hạn sử dụng thuốc thú y, sát trùng.", showIllustration = false) }
                        } else {
                            items(medications, key = { it.id }) { med ->
                                val isExpiringSoon = (med.expiryDateMillis - System.currentTimeMillis()) < 30L * 86_400_000L
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(med.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                        if (isExpiringSoon) StatusBadge("Sắp hết hạn")
                                    }
                                    Text("${med.category} • Còn ${med.quantity} ${med.unit}", style = MaterialTheme.typography.bodySmall)
                                    Text("HSD: ${FarmFormatters.formatDate(med.expiryDateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { viewModel.deleteMedication(med.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddHealth) {
        HealthFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            animals = animals,
            onSave = { viewModel.saveHealth(it); showAddHealth = false },
            onDismiss = { showAddHealth = false }
        )
    }
    if (showAddVaccine) {
        VaccineFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            animals = animals,
            onSave = { viewModel.addVaccination(it); showAddVaccine = false },
            onDismiss = { showAddVaccine = false }
        )
    }
    if (showAddDeworm) {
        DewormFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            animals = animals,
            onSave = { viewModel.addDeworming(it); showAddDeworm = false },
            onDismiss = { showAddDeworm = false }
        )
    }
    if (showAddMed) {
        MedicationFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            onSave = { viewModel.saveMedication(it); showAddMed = false },
            onDismiss = { showAddMed = false }
        )
    }

    deletingHealth?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa hồ sơ sức khỏe",
            message = "Xóa bản ghi triệu chứng \"${target.symptoms}\"?",
            onConfirm = { viewModel.deleteHealth(target.id); deletingHealth = null },
            onDismiss = { deletingHealth = null }
        )
    }
}

@Composable
private fun HealthFormDialog(
    farmId: String,
    animals: List<AnimalEntity>,
    onSave: (HealthRecordEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var animalId by remember { mutableStateOf(animals.firstOrNull()?.id.orEmpty()) }
    var symptoms by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Trung bình") }
    var vetDiagnosis by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var meds by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Đang theo dõi") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi nhận sức khỏe") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (animals.isEmpty()) {
                    Text("Vui lòng thêm vật nuôi trước.", color = MaterialTheme.colorScheme.error)
                } else {
                    val names = animals.map { "${it.code} - ${it.name}" }
                    val sel = animals.firstOrNull { it.id == animalId }?.let { "${it.code} - ${it.name}" } ?: names.first()
                    SimpleDropdownSelector("Vật nuôi *", names, sel, { chosen ->
                        animalId = animals.firstOrNull { "${it.code} - ${it.name}" == chosen }?.id.orEmpty()
                    })
                    PhotoPickerRow(photoUri = photoUri, onPhotoSelected = { photoUri = it }, label = "Ảnh triệu chứng")
                    OutlinedTextField(value = symptoms, onValueChange = { symptoms = it }, label = { Text("Triệu chứng *") })
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả chi tiết") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SimpleDropdownSelector("Mức độ", listOf("Nhẹ", "Trung bình", "Nặng"), severity, { severity = it }, Modifier.weight(1f))
                        SimpleDropdownSelector("Tình trạng", listOf("Đang theo dõi", "Đang điều trị", "Đã hồi phục"), status, { status = it }, Modifier.weight(1f))
                    }
                    OutlinedTextField(value = vetDiagnosis, onValueChange = { vetDiagnosis = it }, label = { Text("Chẩn đoán bác sĩ thú y") })
                    OutlinedTextField(value = treatment, onValueChange = { treatment = it }, label = { Text("Phương án điều trị xác nhận") })
                    OutlinedTextField(value = meds, onValueChange = { meds = it }, label = { Text("Thuốc đã dùng (theo chỉ định)") })
                }
            }
        },
        confirmButton = {
            Button(enabled = animals.isNotEmpty() && symptoms.isNotBlank(), onClick = {
                onSave(
                    HealthRecordEntity(
                        id = "",
                        farmId = farmId,
                        animalId = animalId,
                        detectedDateMillis = System.currentTimeMillis(),
                        symptoms = symptoms.trim(),
                        description = description.trim(),
                        severity = severity,
                        vetDiagnosis = vetDiagnosis.trim(),
                        confirmedTreatment = treatment.trim(),
                        medicationsUsed = meds.trim(),
                        status = status,
                        photoUri = photoUri
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun VaccineFormDialog(
    farmId: String,
    animals: List<AnimalEntity>,
    onSave: (VaccinationRecordEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var animalId by remember { mutableStateOf(animals.firstOrNull()?.id.orEmpty()) }
    var vaccineName by remember { mutableStateOf("") }
    var performer by remember { mutableStateOf("") }
    var followUpDays by remember { mutableStateOf("180") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi nhận tiêm phòng") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (animals.isEmpty()) {
                    Text("Vui lòng thêm vật nuôi trước.", color = MaterialTheme.colorScheme.error)
                } else {
                    val names = animals.map { "${it.code} - ${it.name}" }
                    val sel = animals.firstOrNull { it.id == animalId }?.let { "${it.code} - ${it.name}" } ?: names.first()
                    SimpleDropdownSelector("Vật nuôi *", names, sel, { chosen ->
                        animalId = animals.firstOrNull { "${it.code} - ${it.name}" == chosen }?.id.orEmpty()
                    })
                    OutlinedTextField(value = vaccineName, onValueChange = { vaccineName = it }, label = { Text("Tên Vaccine *") })
                    OutlinedTextField(value = performer, onValueChange = { performer = it }, label = { Text("Người thực hiện") })
                    OutlinedTextField(
                        value = followUpDays,
                        onValueChange = { followUpDays = it },
                        label = { Text("Số ngày nhắc lại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") })
                }
            }
        },
        confirmButton = {
            Button(enabled = animals.isNotEmpty() && vaccineName.isNotBlank(), onClick = {
                val now = System.currentTimeMillis()
                val days = followUpDays.toLongOrNull()
                onSave(
                    VaccinationRecordEntity(
                        id = "",
                        farmId = farmId,
                        animalId = animalId,
                        vaccineName = vaccineName.trim(),
                        vaccinatedDateMillis = now,
                        performer = performer.trim(),
                        nextFollowUpMillis = days?.let { now + it * 86_400_000L },
                        notes = notes.trim()
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun DewormFormDialog(
    farmId: String,
    animals: List<AnimalEntity>,
    onSave: (DewormingRecordEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var animalId by remember { mutableStateOf(animals.firstOrNull()?.id.orEmpty()) }
    var productUsed by remember { mutableStateOf("") }
    var nextDays by remember { mutableStateOf("90") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi nhận tẩy giun") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (animals.isEmpty()) {
                    Text("Vui lòng thêm vật nuôi trước.", color = MaterialTheme.colorScheme.error)
                } else {
                    val names = animals.map { "${it.code} - ${it.name}" }
                    val sel = animals.firstOrNull { it.id == animalId }?.let { "${it.code} - ${it.name}" } ?: names.first()
                    SimpleDropdownSelector("Vật nuôi *", names, sel, { chosen ->
                        animalId = animals.firstOrNull { "${it.code} - ${it.name}" == chosen }?.id.orEmpty()
                    })
                    OutlinedTextField(value = productUsed, onValueChange = { productUsed = it }, label = { Text("Sản phẩm sử dụng *") })
                    OutlinedTextField(
                        value = nextDays,
                        onValueChange = { nextDays = it },
                        label = { Text("Lịch lặp lại (ngày)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") })
                }
            }
        },
        confirmButton = {
            Button(enabled = animals.isNotEmpty() && productUsed.isNotBlank(), onClick = {
                val now = System.currentTimeMillis()
                onSave(
                    DewormingRecordEntity(
                        id = "",
                        farmId = farmId,
                        animalId = animalId,
                        dewormDateMillis = now,
                        productUsed = productUsed.trim(),
                        nextScheduleMillis = nextDays.toLongOrNull()?.let { now + it * 86_400_000L },
                        notes = notes.trim()
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun MedicationFormDialog(
    farmId: String,
    onSave: (MedicationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sát trùng - Vệ sinh") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("lọ") }
    var validDays by remember { mutableStateOf("180") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thuốc vào kho") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên thuốc / chế phẩm *") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Phân loại") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Số lượng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Đơn vị") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = validDays,
                    onValueChange = { validDays = it },
                    label = { Text("Hạn sử dụng còn (ngày)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") })
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = {
                val now = System.currentTimeMillis()
                val days = validDays.toLongOrNull() ?: 180L
                onSave(
                    MedicationEntity(
                        id = "",
                        farmId = farmId,
                        name = name.trim(),
                        category = category.trim(),
                        quantity = quantity.toDoubleOrNull() ?: 1.0,
                        unit = unit.trim(),
                        intakeDateMillis = now,
                        expiryDateMillis = now + days * 86_400_000L,
                        notes = notes.trim()
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
