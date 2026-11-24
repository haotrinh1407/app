package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.AnimalEntity
import com.example.data.local.BarnEntity
import com.example.data.local.FinancialTransactionEntity
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmDanger
import com.example.ui.theme.FarmSuccess
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun FinanceScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val activeFarm by viewModel.activeFarm.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val animals by viewModel.animals.collectAsState()
    val barns by viewModel.barns.collectAsState()

    var filterType by remember { mutableStateOf("Tất cả") } // Tất cả | Chi phí | Doanh thu
    var showAddDialog by remember { mutableStateOf(false) }
    var deletingTx by remember { mutableStateOf<FinancialTransactionEntity?>(null) }

    val totalIncome = remember(transactions) { transactions.filter { it.txType == "INCOME" }.sumOf { it.amount } }
    val totalExpense = remember(transactions) { transactions.filter { it.txType == "EXPENSE" }.sumOf { it.amount } }
    val profit = totalIncome - totalExpense

    val monthlyBars = remember(transactions) {
        transactions.groupBy { FarmFormatters.formatMonthYear(it.txDateMillis) }
            .entries
            .take(5)
            .map { (month, list) ->
                val inc = list.filter { it.txType == "INCOME" }.sumOf { it.amount }
                val exp = list.filter { it.txType == "EXPENSE" }.sumOf { it.amount }
                Triple(month, inc, exp)
            }
    }

    val barnMap = remember(barns) { barns.associateBy { it.id } }
    val animalMap = remember(animals) { animals.associateBy { it.id } }

    val filtered = remember(transactions, filterType) {
        when (filterType) {
            "Chi phí" -> transactions.filter { it.txType == "EXPENSE" }
            "Doanh thu" -> transactions.filter { it.txType == "INCOME" }
            else -> transactions
        }
    }

    Scaffold(
        topBar = { FarmTopBar("Tài chính thu chi", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ghi nhận thu chi")
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
                    StatMetricTile("Tổng doanh thu", FarmFormatters.formatVnd(totalIncome), Icons.Default.TrendingUp, FarmSuccess, Modifier.weight(1f))
                    StatMetricTile("Tổng chi phí", FarmFormatters.formatVnd(totalExpense), Icons.Default.TrendingDown, FarmDanger, Modifier.weight(1f))
                }
            }
            item {
                StatMetricTile(
                    label = "Lợi nhuận tạm tính",
                    value = FarmFormatters.formatVnd(profit),
                    icon = Icons.Default.AccountBalanceWallet,
                    tint = if (profit >= 0) FarmSuccess else FarmDanger
                )
            }

            if (monthlyBars.isNotEmpty()) {
                item {
                    FarmCard {
                        Text("Biểu đồ thu chi theo tháng", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        val maxVal = monthlyBars.maxOf { maxOf(it.second, it.third) }.coerceAtLeast(1L).toFloat()
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            val groupWidth = size.width / monthlyBars.size
                            val barW = (groupWidth * 0.28f).coerceAtMost(36f)
                            monthlyBars.forEachIndexed { idx, (_, inc, exp) ->
                                val centerX = idx * groupWidth + groupWidth / 2f
                                val incH = (inc / maxVal) * (size.height - 16f)
                                val expH = (exp / maxVal) * (size.height - 16f)
                                drawRect(
                                    color = FarmSuccess,
                                    topLeft = Offset(centerX - barW - 4f, size.height - incH),
                                    size = Size(barW, incH)
                                )
                                drawRect(
                                    color = FarmDanger,
                                    topLeft = Offset(centerX + 4f, size.height - expH),
                                    size = Size(barW, expH)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            monthlyBars.forEach { (month, _, _) ->
                                Text(month, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("● Doanh thu", color = FarmSuccess, style = MaterialTheme.typography.labelMedium)
                            Text("● Chi phí", color = FarmDanger, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            item {
                FilterChipRow(
                    options = listOf("Tất cả", "Chi phí", "Doanh thu"),
                    selected = filterType,
                    onSelected = { filterType = it }
                )
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Chưa có giao dịch tài chính",
                        subtitle = "Ghi nhận chi phí thức ăn, vaccine hoặc doanh thu bán vật nuôi để theo dõi lợi nhuận.",
                        actionLabel = "Ghi nhận thu chi",
                        onAction = { showAddDialog = true },
                        showIllustration = false
                    )
                }
            } else {
                items(filtered, key = { it.id }) { tx ->
                    val isIncome = tx.txType == "INCOME"
                    FarmCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(tx.category)
                            Text(
                                text = (if (isIncome) "+" else "-") + FarmFormatters.formatVnd(tx.amount),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isIncome) FarmSuccess else FarmDanger
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(tx.description, style = MaterialTheme.typography.titleSmall)
                        val linked = listOfNotNull(
                            barnMap[tx.barnId]?.name,
                            animalMap[tx.animalId]?.name
                        ).joinToString(" • ")
                        Text(
                            buildString {
                                append(FarmFormatters.formatDate(tx.txDateMillis))
                                if (linked.isNotBlank()) append(" • $linked")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!tx.receiptPhotoUri.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = tx.receiptPhotoUri,
                                contentDescription = "Ảnh hóa đơn",
                                modifier = Modifier.fillMaxWidth().height(110.dp).clip(MaterialTheme.shapes.small),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { deletingTx = tx }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TransactionFormDialog(
            farmId = activeFarm?.id.orEmpty(),
            animals = animals,
            barns = barns,
            onSave = { viewModel.saveTransaction(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    deletingTx?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa giao dịch",
            message = "Xóa khoản \"${target.description}\" (${FarmFormatters.formatVnd(target.amount)})?",
            onConfirm = { viewModel.deleteTransaction(target.id); deletingTx = null },
            onDismiss = { deletingTx = null }
        )
    }
}

@Composable
private fun TransactionFormDialog(
    farmId: String,
    animals: List<AnimalEntity>,
    barns: List<BarnEntity>,
    onSave: (FinancialTransactionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var txType by remember { mutableStateOf("EXPENSE") }
    var category by remember { mutableStateOf(FarmConstants.EXPENSE_CATEGORIES.first()) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedAnimalId by remember { mutableStateOf<String?>(null) }
    var selectedBarnId by remember { mutableStateOf<String?>(null) }
    var receiptUri by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi nhận thu / chi") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = txType == "EXPENSE",
                        onClick = { txType = "EXPENSE"; category = FarmConstants.EXPENSE_CATEGORIES.first() },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Chi phí") }
                    SegmentedButton(
                        selected = txType == "INCOME",
                        onClick = { txType = "INCOME"; category = FarmConstants.INCOME_CATEGORIES.first() },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("Doanh thu") }
                }
                val cats = if (txType == "EXPENSE") FarmConstants.EXPENSE_CATEGORIES else FarmConstants.INCOME_CATEGORIES
                SimpleDropdownSelector("Danh mục", cats, category) { category = it }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Số tiền (VNĐ) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Nội dung *") })
                if (barns.isNotEmpty()) {
                    val bLabels = listOf("Không chọn") + barns.map { it.name }
                    val bSel = barns.firstOrNull { it.id == selectedBarnId }?.name ?: "Không chọn"
                    SimpleDropdownSelector("Chuồng liên quan", bLabels, bSel) { chosen ->
                        selectedBarnId = barns.firstOrNull { it.name == chosen }?.id
                    }
                }
                if (animals.isNotEmpty()) {
                    val aLabels = listOf("Không chọn") + animals.map { "${it.code} - ${it.name}" }
                    val aSel = animals.firstOrNull { it.id == selectedAnimalId }?.let { "${it.code} - ${it.name}" } ?: "Không chọn"
                    SimpleDropdownSelector("Vật nuôi liên quan", aLabels, aSel) { chosen ->
                        selectedAnimalId = animals.firstOrNull { "${it.code} - ${it.name}" == chosen }?.id
                    }
                }
                PhotoPickerRow(photoUri = receiptUri, onPhotoSelected = { receiptUri = it }, label = "Ảnh hóa đơn (tùy chọn)")
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") })
            }
        },
        confirmButton = {
            val amt = amountText.toLongOrNull() ?: 0L
            Button(enabled = amt > 0L && description.isNotBlank(), onClick = {
                onSave(
                    FinancialTransactionEntity(
                        id = "",
                        farmId = farmId,
                        txType = txType,
                        category = category,
                        amount = amt,
                        txDateMillis = System.currentTimeMillis(),
                        description = description.trim(),
                        animalId = selectedAnimalId,
                        barnId = selectedBarnId,
                        receiptPhotoUri = receiptUri,
                        notes = notes.trim()
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
