package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun FeedScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val library by viewModel.feedLibrary.collectAsState()
    val rations by viewModel.feedRations.collectAsState()
    val inventory by viewModel.feedInventory.collectAsState()
    val usageList by viewModel.feedUsage.collectAsState()
    val barns by viewModel.barns.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var categoryFilter by remember { mutableStateOf("Tất cả") }
    var showAddRation by remember { mutableStateOf(false) }
    var showAddInventory by remember { mutableStateOf(false) }
    var showAddUsage by remember { mutableStateOf(false) }

    val barnMap = remember(barns) { barns.associateBy { it.id } }

    Scaffold(
        topBar = { FarmTopBar("Thức ăn & Dinh dưỡng", onBack = onBack) },
        floatingActionButton = {
            if (selectedTab > 0) {
                FloatingActionButton(onClick = {
                    when (selectedTab) {
                        1 -> showAddRation = true
                        2 -> showAddInventory = true
                        3 -> showAddUsage = true
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm mới")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Thư viện") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Khẩu phần") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Kho thức ăn") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Tiêu thụ") })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 84.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            FilterChipRow(
                                options = listOf("Tất cả") + FarmConstants.FEED_CATEGORIES,
                                selected = categoryFilter,
                                onSelected = { categoryFilter = it }
                            )
                        }
                        val filteredLib = if (categoryFilter == "Tất cả") library else library.filter { it.category == categoryFilter }
                        items(filteredLib, key = { it.id }) { item ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    StatusBadge(item.category)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.description, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(4.dp))
                                Text("Phù hợp: ${item.suitableSpecies}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text("Dinh dưỡng: ${item.nutritionInfo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Cách dùng: ${item.usageGuide}", style = MaterialTheme.typography.bodySmall)
                                Text("Bảo quản: ${item.storageGuide}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    1 -> {
                        if (rations.isEmpty()) {
                            item { EmptyStateView("Chưa có khẩu phần", "Lưu khẩu phần theo loài, cân nặng và giai đoạn sinh trưởng.", "Thêm khẩu phần", { showAddRation = true }, false) }
                        } else {
                            items(rations, key = { it.id }) { rat ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${rat.species} (${rat.growthStage})", style = MaterialTheme.typography.titleMedium)
                                        StatusBadge("${rat.dailyAmountKg} kg/ngày")
                                    }
                                    Text("Giống: ${rat.breed.ifBlank { "Chung" }} • Tuổi: ${rat.ageStage} • Trọng lượng: ${rat.weightRange}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Text(rat.rationSummary, style = MaterialTheme.typography.bodyMedium)
                                    if (rat.notes.isNotBlank()) Text("Lưu ý: ${rat.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { viewModel.deleteFeedRation(rat.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        if (inventory.isEmpty()) {
                            item { EmptyStateView("Kho thức ăn đang trống", "Nhập số lượng thức ăn, giá nhập và hạn sử dụng để theo dõi.", "Nhập kho", { showAddInventory = true }, false) }
                        } else {
                            items(inventory, key = { it.id }) { inv ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(inv.feedName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        StatusBadge("${inv.quantity} ${inv.unit}")
                                    }
                                    Text("${inv.category} • Giá: ${FarmFormatters.formatVnd(inv.unitPrice)}/${inv.unit}", style = MaterialTheme.typography.bodySmall)
                                    Text("Ngày nhập: ${FarmFormatters.formatDate(inv.intakeDateMillis)} • NCC: ${inv.supplier.ifBlank { "Không rõ" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { viewModel.deleteFeedInventory(inv.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        if (usageList.isEmpty()) {
                            item { EmptyStateView("Chưa có nhật ký tiêu thụ", "Ghi nhận lượng thức ăn tiêu thụ hàng ngày theo chuồng.", "Ghi tiêu thụ", { showAddUsage = true }, false) }
                        } else {
                            items(usageList, key = { it.id }) { u ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${u.feedName} (${u.amount} ${u.unit})", style = MaterialTheme.typography.titleSmall)
                                        Text(FarmFormatters.formatVnd(u.cost), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("Ngày: ${FarmFormatters.formatDate(u.usageDateMillis)} • Chuồng: ${barnMap[u.barnId]?.name ?: "Toàn trại"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRation) {
        var species by remember { mutableStateOf("Bò") }
        var breed by remember { mutableStateOf("") }
        var ageStage by remember { mutableStateOf("Trưởng thành") }
        var weightRange by remember { mutableStateOf("") }
        var growthStage by remember { mutableStateOf("Sinh trưởng") }
        var summary by remember { mutableStateOf("") }
        var dailyKg by remember { mutableStateOf("10") }

        AlertDialog(
            onDismissRequest = { showAddRation = false },
            title = { Text("Thêm khẩu phần ăn") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimpleDropdownSelector("Loài", FarmConstants.SPECIES_LIST, species) { species = it }
                    SimpleDropdownSelector("Giai đoạn", listOf("Sinh trưởng", "Mang thai", "Sau sinh", "Vỗ béo"), growthStage) { growthStage = it }
                    OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Giống") }, singleLine = true)
                    OutlinedTextField(value = ageStage, onValueChange = { ageStage = it }, label = { Text("Độ tuổi") }, singleLine = true)
                    OutlinedTextField(value = weightRange, onValueChange = { weightRange = it }, label = { Text("Khoảng cân nặng") }, singleLine = true)
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Thành phần khẩu phần *") })
                    OutlinedTextField(
                        value = dailyKg,
                        onValueChange = { dailyKg = it },
                        label = { Text("Tổng lượng/ngày (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(enabled = summary.isNotBlank(), onClick = {
                    viewModel.saveFeedRation(
                        FeedRationEntity(
                            id = "",
                            farmId = activeFarm?.id.orEmpty(),
                            species = species,
                            breed = breed.trim(),
                            ageStage = ageStage.trim(),
                            weightRange = weightRange.trim(),
                            growthStage = growthStage,
                            rationSummary = summary.trim(),
                            dailyAmountKg = dailyKg.toDoubleOrNull() ?: 5.0
                        )
                    )
                    showAddRation = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showAddRation = false }) { Text("Hủy") } }
        )
    }

    if (showAddInventory) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(FarmConstants.FEED_CATEGORIES.first()) }
        var qty by remember { mutableStateOf("50") }
        var unit by remember { mutableStateOf("kg") }
        var price by remember { mutableStateOf("12000") }
        var supplier by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddInventory = false },
            title = { Text("Nhập kho thức ăn") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên thức ăn *") }, singleLine = true)
                    SimpleDropdownSelector("Loại", FarmConstants.FEED_CATEGORIES, category) { category = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Số lượng") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Đơn vị") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Đơn giá (đ)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Nhà cung cấp") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(enabled = name.isNotBlank(), onClick = {
                    viewModel.saveFeedInventory(
                        FeedInventoryEntity(
                            id = "",
                            farmId = activeFarm?.id.orEmpty(),
                            feedName = name.trim(),
                            category = category,
                            quantity = qty.toDoubleOrNull() ?: 0.0,
                            unit = unit.trim(),
                            unitPrice = price.toLongOrNull() ?: 0L,
                            intakeDateMillis = System.currentTimeMillis(),
                            supplier = supplier.trim()
                        )
                    )
                    showAddInventory = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showAddInventory = false }) { Text("Hủy") } }
        )
    }

    if (showAddUsage) {
        var feedName by remember { mutableStateOf(inventory.firstOrNull()?.feedName ?: "") }
        var amount by remember { mutableStateOf("10") }
        var cost by remember { mutableStateOf("120000") }
        var selectedBarnId by remember { mutableStateOf(barns.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showAddUsage = false },
            title = { Text("Ghi nhận tiêu thụ thức ăn") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = feedName, onValueChange = { feedName = it }, label = { Text("Loại thức ăn *") }, singleLine = true)
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Lượng dùng (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Chi phí ước tính (đ)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    if (barns.isNotEmpty()) {
                        val bLabels = listOf("Toàn trang trại") + barns.map { it.name }
                        val bSel = barns.firstOrNull { it.id == selectedBarnId }?.name ?: "Toàn trang trại"
                        SimpleDropdownSelector("Chuồng sử dụng", bLabels, bSel) { chosen ->
                            selectedBarnId = barns.firstOrNull { it.name == chosen }?.id
                        }
                    }
                }
            },
            confirmButton = {
                Button(enabled = feedName.isNotBlank(), onClick = {
                    viewModel.addFeedUsage(
                        FeedUsageEntity(
                            id = "",
                            farmId = activeFarm?.id.orEmpty(),
                            feedName = feedName.trim(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            usageDateMillis = System.currentTimeMillis(),
                            barnId = selectedBarnId,
                            cost = cost.toLongOrNull() ?: 0L
                        )
                    )
                    showAddUsage = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showAddUsage = false }) { Text("Hủy") } }
        )
    }
}
