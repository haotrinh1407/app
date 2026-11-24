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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.AnimalEntity
import com.example.data.local.BarnEntity
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun AnimalsScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit,
    onOpenAnimal: (String) -> Unit
) {
    val animals by viewModel.animals.collectAsState()
    val barns by viewModel.barns.collectAsState()
    val activeFarm by viewModel.activeFarm.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var query by remember { mutableStateOf("") }
    var speciesFilter by remember { mutableStateOf("Tất cả") }
    var genderFilter by remember { mutableStateOf("Tất cả") }
    var statusFilter by remember { mutableStateOf("Tất cả") }
    var barnFilterId by remember { mutableStateOf("Tất cả") }
    var ageFilter by remember { mutableStateOf("Tất cả") }
    var sortBy by remember { mutableStateOf("Ngày nhập mới") }
    var showFilters by remember { mutableStateOf(false) }

    var editingAnimal by remember { mutableStateOf<AnimalEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var deletingAnimal by remember { mutableStateOf<AnimalEntity?>(null) }

    val now = System.currentTimeMillis()
    val sixMonthsMs = 180L * 86_400_000L

    val filtered = remember(
        animals, query, speciesFilter, genderFilter, statusFilter, barnFilterId, ageFilter, sortBy
    ) {
        animals.filter { animal ->
            val matchQuery = query.isBlank() ||
                animal.name.contains(query, true) ||
                animal.code.contains(query, true) ||
                animal.breed.contains(query, true)
            val matchSpecies = speciesFilter == "Tất cả" || animal.species == speciesFilter
            val matchGender = genderFilter == "Tất cả" || animal.gender == genderFilter
            val matchStatus = statusFilter == "Tất cả" || animal.status == statusFilter
            val matchBarn = barnFilterId == "Tất cả" || animal.barnId == barnFilterId
            val matchAge = when (ageFilter) {
                "Con non (<6 tháng)" -> animal.birthDateMillis != null && (now - animal.birthDateMillis) < sixMonthsMs
                "Trưởng thành" -> animal.birthDateMillis == null || (now - animal.birthDateMillis) >= sixMonthsMs
                else -> true
            }
            matchQuery && matchSpecies && matchGender && matchStatus && matchBarn && matchAge
        }.let { list ->
            when (sortBy) {
                "Cân nặng giảm dần" -> list.sortedByDescending { it.weightKg }
                "Cân nặng tăng dần" -> list.sortedBy { it.weightKg }
                "Ngày sinh mới" -> list.sortedByDescending { it.birthDateMillis ?: 0L }
                else -> list.sortedByDescending { it.intakeDateMillis }
            }
        }
    }

    val barnNameMap = remember(barns) { barns.associateBy({ it.id }, { it.name }) }

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Quản lý vật nuôi (${filtered.size})",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Bộ lọc nâng cao")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAnimal = null
                    showFormDialog = true
                },
                modifier = Modifier.testTag("add_animal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm vật nuôi")
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
            FarmSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Tìm theo tên, mã số, giống..."
            )

            FilterChipRow(
                options = listOf("Tất cả") + FarmConstants.SPECIES_LIST,
                selected = speciesFilter,
                onSelected = { speciesFilter = it }
            )

            if (showFilters) {
                FarmCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Bộ lọc & Sắp xếp chi tiết", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SimpleDropdownSelector(
                                label = "Giới tính",
                                options = listOf("Tất cả") + FarmConstants.GENDER_LIST,
                                selected = genderFilter,
                                onSelected = { genderFilter = it },
                                modifier = Modifier.weight(1f)
                            )
                            SimpleDropdownSelector(
                                label = "Trạng thái",
                                options = listOf("Tất cả") + FarmConstants.ANIMAL_STATUSES,
                                selected = statusFilter,
                                onSelected = { statusFilter = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val barnOptions = listOf("Tất cả") + barns.map { it.name }
                            val selectedBarnName = if (barnFilterId == "Tất cả") "Tất cả" else barnNameMap[barnFilterId] ?: "Tất cả"
                            SimpleDropdownSelector(
                                label = "Chuồng",
                                options = barnOptions,
                                selected = selectedBarnName,
                                onSelected = { chosen ->
                                    barnFilterId = if (chosen == "Tất cả") "Tất cả"
                                    else barns.firstOrNull { it.name == chosen }?.id ?: "Tất cả"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SimpleDropdownSelector(
                                label = "Độ tuổi",
                                options = listOf("Tất cả", "Con non (<6 tháng)", "Trưởng thành"),
                                selected = ageFilter,
                                onSelected = { ageFilter = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        SimpleDropdownSelector(
                            label = "Sắp xếp theo",
                            options = listOf("Ngày nhập mới", "Ngày sinh mới", "Cân nặng giảm dần", "Cân nặng tăng dần"),
                            selected = sortBy,
                            onSelected = { sortBy = it }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                EmptyStateView(
                    title = "Chưa có vật nuôi phù hợp",
                    subtitle = "Hãy thêm hồ sơ vật nuôi mới hoặc thay đổi điều kiện tìm kiếm/lọc.",
                    actionLabel = "Thêm vật nuôi",
                    onAction = {
                        editingAnimal = null
                        showFormDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { animal ->
                        FarmCard(
                            onClick = { onOpenAnimal(animal.id) },
                            modifier = Modifier.testTag("animal_card_${animal.code}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!animal.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = animal.photoUri,
                                        contentDescription = animal.name,
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Pets,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${animal.code} • ${animal.species}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        StatusBadge(animal.status)
                                    }
                                    Text(
                                        text = animal.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = buildString {
                                            append("${animal.gender} • ${FarmFormatters.formatWeight(animal.weightKg, settings.weightUnit)}")
                                            barnNameMap[animal.barnId]?.let { append(" • $it") }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    editingAnimal = animal
                                    showFormDialog = true
                                }) {
                                    Text("Sửa")
                                }
                                TextButton(onClick = { viewModel.archiveAnimal(animal.id) }) {
                                    Text("Lưu trữ")
                                }
                                TextButton(onClick = { deletingAnimal = animal }) {
                                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        AnimalFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            barns = barns,
            initial = editingAnimal,
            onSave = { animal, isNew ->
                viewModel.saveAnimal(animal, isNew)
                showFormDialog = false
            },
            onDismiss = { showFormDialog = false }
        )
    }

    deletingAnimal?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa hồ sơ vật nuôi",
            message = "Bạn chắc chắn muốn xóa ${target.name} (${target.code})? Thao tác này không thể hoàn tác.",
            onConfirm = {
                viewModel.deleteAnimal(target.id)
                deletingAnimal = null
            },
            onDismiss = { deletingAnimal = null }
        )
    }
}

@Composable
fun AnimalFormDialog(
    farmId: String,
    barns: List<BarnEntity>,
    initial: AnimalEntity?,
    onSave: (AnimalEntity, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isNew = initial == null
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var species by remember { mutableStateOf(initial?.species ?: "Dê") }
    var breed by remember { mutableStateOf(initial?.breed ?: "") }
    var gender by remember { mutableStateOf(initial?.gender ?: "Cái") }
    var weightText by remember { mutableStateOf(initial?.weightKg?.takeIf { it > 0 }?.toString() ?: "") }
    var origin by remember { mutableStateOf(initial?.origin ?: "") }
    var priceText by remember { mutableStateOf(initial?.purchasePrice?.takeIf { it > 0 }?.toString() ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: "Khỏe mạnh") }
    var caretaker by remember { mutableStateOf(initial?.caretaker ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var photoUri by remember { mutableStateOf(initial?.photoUri) }
    var selectedBarnId by remember { mutableStateOf(initial?.barnId ?: barns.firstOrNull()?.id) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Thêm vật nuôi mới" else "Cập nhật hồ sơ vật nuôi") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!validationError.isNullOrBlank()) {
                    Text(validationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                PhotoPickerRow(photoUri = photoUri, onPhotoSelected = { photoUri = it }, label = "Ảnh vật nuôi")
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Mã vật nuôi *") },
                    placeholder = { Text("VD: DE-005") },
                    modifier = Modifier.fillMaxWidth().testTag("animal_code_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên hoặc mô tả *") },
                    modifier = Modifier.fillMaxWidth().testTag("animal_name_input"),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimpleDropdownSelector(
                        label = "Loài",
                        options = FarmConstants.SPECIES_LIST,
                        selected = species,
                        onSelected = { species = it },
                        modifier = Modifier.weight(1f)
                    )
                    SimpleDropdownSelector(
                        label = "Giới tính",
                        options = FarmConstants.GENDER_LIST,
                        selected = gender,
                        onSelected = { gender = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Giống (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Cân nặng (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Giá mua (đ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                if (barns.isNotEmpty()) {
                    val barnLabels = listOf("Chưa xếp chuồng") + barns.map { it.name }
                    val currentLabel = barns.firstOrNull { it.id == selectedBarnId }?.name ?: "Chưa xếp chuồng"
                    SimpleDropdownSelector(
                        label = "Chuồng hiện tại",
                        options = barnLabels,
                        selected = currentLabel,
                        onSelected = { label ->
                            selectedBarnId = barns.firstOrNull { it.name == label }?.id
                        }
                    )
                }
                SimpleDropdownSelector(
                    label = "Trạng thái",
                    options = FarmConstants.ANIMAL_STATUSES,
                    selected = status,
                    onSelected = { status = it }
                )
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Nguồn gốc") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = caretaker,
                    onValueChange = { caretaker = it },
                    label = { Text("Người phụ trách") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isBlank() || name.isBlank()) {
                        validationError = "Vui lòng nhập mã vật nuôi và tên."
                        return@Button
                    }
                    val entity = (initial ?: AnimalEntity(
                        id = "",
                        farmId = farmId,
                        code = code.trim(),
                        name = name.trim(),
                        species = species
                    )).copy(
                        barnId = selectedBarnId,
                        code = code.trim(),
                        name = name.trim(),
                        species = species,
                        breed = breed.trim(),
                        gender = gender,
                        weightKg = weightText.toDoubleOrNull() ?: 0.0,
                        origin = origin.trim(),
                        purchasePrice = priceText.toLongOrNull() ?: 0L,
                        status = status,
                        caretaker = caretaker.trim(),
                        notes = notes.trim(),
                        photoUri = photoUri
                    )
                    onSave(entity, isNew)
                },
                modifier = Modifier.testTag("save_animal_button")
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
