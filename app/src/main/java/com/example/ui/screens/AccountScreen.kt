package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.FarmEntity
import com.example.data.repository.FarmConstants
import com.example.ui.components.*
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(
    viewModel: FarmViewModel,
    onOpenAdmin: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val farms by viewModel.farms.collectAsState()
    val activeFarm by viewModel.activeFarm.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showFarmDialog by remember { mutableStateOf(false) }
    var editingFarm by remember { mutableStateOf<FarmEntity?>(null) }
    var deletingFarm by remember { mutableStateOf<FarmEntity?>(null) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { FarmTopBar("Tài khoản & Cài đặt") }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Card
            item {
                FarmCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(user?.fullName ?: "Người dùng", style = MaterialTheme.typography.titleLarge)
                            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!user?.phone.isNullOrBlank()) {
                                Text("SĐT: ${user?.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusBadge(if (user?.role == "ADMIN") "Quản trị viên" else "Hộ chăn nuôi")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showProfileDialog = true }, modifier = Modifier.weight(1f).heightIn(min = 46.dp)) {
                            Text("Sửa hồ sơ")
                        }
                        OutlinedButton(onClick = { showPasswordDialog = true }, modifier = Modifier.weight(1f).heightIn(min = 46.dp)) {
                            Text("Đổi mật khẩu")
                        }
                    }
                }
            }

            // Admin Access (strictly hidden from standard USER)
            if (user?.role == "ADMIN") {
                item {
                    Button(
                        onClick = onOpenAdmin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("open_admin_dashboard")
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mở Admin Dashboard")
                    }
                }
            }

            // Farms Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trang trại của tôi (${farms.size})", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { editingFarm = null; showFarmDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Thêm trang trại")
                    }
                }
            }
            items(farms, key = { it.id }) { farm ->
                val isActive = farm.id == activeFarm?.id
                FarmCard(
                    onClick = { viewModel.selectActiveFarm(farm.id) },
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(farm.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${farm.farmingType} • ${farm.scale}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (farm.address.isNotBlank()) {
                                Text(farm.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (isActive) StatusBadge("Đang chọn")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingFarm = farm; showFarmDialog = true }) { Text("Sửa") }
                        if (farms.size > 1) {
                            TextButton(onClick = { deletingFarm = farm }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }

            // Settings Card
            item {
                Text("Cài đặt ứng dụng", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                FarmCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Thông báo nhắc lịch", style = MaterialTheme.typography.titleSmall)
                            Text("Hiển thị nhắc nhở lịch chăm sóc nội bộ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Cỡ chữ lớn dễ đọc", style = MaterialTheme.typography.titleSmall)
                            Text("Phù hợp cho người lớn tuổi xem ngoài trời", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.largeText,
                            onCheckedChange = { viewModel.setLargeText(it) }
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Chế độ hiển thị", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    FilterChipRow(
                        options = listOf("SYSTEM", "LIGHT", "DARK"),
                        selected = settings.themeMode,
                        onSelected = { viewModel.setThemeMode(it) }
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Đơn vị khối lượng", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    FilterChipRow(
                        options = listOf("kg", "tấn"),
                        selected = settings.weightUnit,
                        onSelected = { viewModel.setWeightUnit(it) }
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenNotifications, modifier = Modifier.weight(1f).heightIn(min = 46.dp)) {
                            Text("Thông báo")
                        }
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.buildExportSummaryText()
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Sao lưu dữ liệu FarmCare")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(intent, "Xuất / Sao lưu dữ liệu"))
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                        ) {
                            Text("Sao lưu JSON")
                        }
                    }
                }
            }

            // Support & FAQ
            item {
                Text("Hỗ trợ & Hướng dẫn", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                FarmCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showFaqDialog = true }, modifier = Modifier.weight(1f).heightIn(min = 46.dp)) {
                            Text("Hướng dẫn & FAQ")
                        }
                        OutlinedButton(onClick = { showFeedbackDialog = true }, modifier = Modifier.weight(1f).heightIn(min = 46.dp)) {
                            Text("Góp ý / Báo lỗi")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Liên hệ hỗ trợ kỹ thuật: hotro@farmcare.vn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Logout
            item {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("logout_button")
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất")
                }
            }
        }
    }

    if (showProfileDialog && user != null) {
        val current = user!!
        var fullName by remember { mutableStateOf(current.fullName) }
        var phone by remember { mutableStateOf(current.phone) }
        var selectedSpecies by remember {
            mutableStateOf(current.preferredSpeciesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet())
        }
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Cập nhật hồ sơ") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Họ và tên *") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Số điện thoại") })
                    Text("Vật nuôi ưu tiên hiển thị:", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FarmConstants.SPECIES_LIST.forEach { sp ->
                            FilterChip(
                                selected = sp in selectedSpecies,
                                onClick = {
                                    selectedSpecies = if (sp in selectedSpecies) selectedSpecies - sp else selectedSpecies + sp
                                },
                                label = { Text(sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(enabled = fullName.isNotBlank(), onClick = {
                    viewModel.updateProfile(
                        current.copy(
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            preferredSpeciesCsv = selectedSpecies.joinToString(",")
                        )
                    )
                    showProfileDialog = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showProfileDialog = false }) { Text("Hủy") } }
        )
    }

    if (showPasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var msg by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Đổi mật khẩu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!msg.isNullOrBlank()) Text(msg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = oldPass, onValueChange = { oldPass = it }, label = { Text("Mật khẩu hiện tại") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Mật khẩu mới (từ 6 ký tự)") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.changePassword(oldPass, newPass) { ok, info ->
                        if (ok) {
                            viewModel.notify(info)
                            showPasswordDialog = false
                        } else {
                            msg = info
                        }
                    }
                }) { Text("Cập nhật") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Hủy") } }
        )
    }

    if (showFarmDialog && user != null) {
        val current = editingFarm
        var name by remember { mutableStateOf(current?.name ?: "") }
        var address by remember { mutableStateOf(current?.address ?: "") }
        var farmingType by remember { mutableStateOf(current?.farmingType ?: "Chăn nuôi tổng hợp") }
        var scale by remember { mutableStateOf(current?.scale ?: "Hộ chăn nuôi nhỏ") }
        var notes by remember { mutableStateOf(current?.notes ?: "") }
        AlertDialog(
            onDismissRequest = { showFarmDialog = false },
            title = { Text(if (current == null) "Thêm trang trại" else "Sửa trang trại") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên trang trại *") })
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Địa chỉ") })
                    OutlinedTextField(value = farmingType, onValueChange = { farmingType = it }, label = { Text("Loại chăn nuôi") })
                    SimpleDropdownSelector("Quy mô", listOf("Hộ chăn nuôi nhỏ", "Hộ chăn nuôi vừa", "Trang trại tập trung"), scale) { scale = it }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ghi chú") })
                }
            },
            confirmButton = {
                Button(enabled = name.isNotBlank(), onClick = {
                    viewModel.saveFarm(
                        (current ?: FarmEntity(id = "", ownerUserId = user!!.id, name = name.trim())).copy(
                            name = name.trim(),
                            address = address.trim(),
                            farmingType = farmingType.trim(),
                            scale = scale,
                            notes = notes.trim()
                        )
                    )
                    showFarmDialog = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showFarmDialog = false }) { Text("Hủy") } }
        )
    }

    deletingFarm?.let { target ->
        ConfirmDeleteDialog(
            title = "Xóa trang trại",
            message = "Xóa trang trại \"${target.name}\" cùng toàn bộ dữ liệu liên quan?",
            onConfirm = { viewModel.deleteFarm(target.id); deletingFarm = null },
            onDismiss = { deletingFarm = null }
        )
    }

    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("Hướng dẫn & Câu hỏi thường gặp") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Bắt đầu quản lý đàn", fontWeight = FontWeight.SemiBold)
                    Text("Vào Quản lý -> Quản lý chuồng trại để tạo chuồng, sau đó vào Quản lý vật nuôi để thêm từng cá thể hoặc theo đàn.")
                    Text("2. Theo dõi sinh sản & sức khỏe", fontWeight = FontWeight.SemiBold)
                    Text("Ghi nhận phối giống để hệ thống tự tính ngày sinh dự kiến. Luôn hỏi bác sĩ thú y khi có triệu chứng nặng.")
                    Text("3. Trợ lý AI", fontWeight = FontWeight.SemiBold)
                    Text("Hiện đang chạy chế độ mẫu nội bộ tra cứu theo thư viện kiến thức. Bật Firebase để dùng Gemini AI đầy đủ.")
                }
            },
            confirmButton = { TextButton(onClick = { showFaqDialog = false }) { Text("Đã hiểu") } }
        )
    }

    if (showFeedbackDialog) {
        var fbType by remember { mutableStateOf("Góp ý") }
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Gửi phản hồi / Báo lỗi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimpleDropdownSelector("Loại", listOf("Góp ý", "Báo lỗi"), fbType) { fbType = it }
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Tiêu đề *") })
                    OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Nội dung chi tiết *") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(enabled = subject.isNotBlank() && message.isNotBlank(), onClick = {
                    viewModel.submitFeedback(fbType, subject.trim(), message.trim())
                    showFeedbackDialog = false
                }) { Text("Gửi") }
            },
            dismissButton = { TextButton(onClick = { showFeedbackDialog = false }) { Text("Hủy") } }
        )
    }
}
