package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.FarmConstants
import com.example.ui.components.FarmCard
import com.example.ui.viewmodel.FarmViewModel

private enum class AuthTab { LOGIN, REGISTER, FORGOT }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(viewModel: FarmViewModel) {
    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var farmName by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf(setOf("Dê", "Bò", "Heo", "Gà")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.widthIn(max = 520.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header Hero
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                            .clip(MaterialTheme.shapes.large)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = stringResource(R.string.cd_hero_banner),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "FarmCare — Trợ lý chăn nuôi",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = activeTab == AuthTab.LOGIN,
                            onClick = { activeTab = AuthTab.LOGIN; errorMessage = null; infoMessage = null },
                            shape = SegmentedButtonDefaults.itemShape(0, 3),
                            modifier = Modifier.testTag("tab_login")
                        ) { Text("Đăng nhập") }
                        SegmentedButton(
                            selected = activeTab == AuthTab.REGISTER,
                            onClick = { activeTab = AuthTab.REGISTER; errorMessage = null; infoMessage = null },
                            shape = SegmentedButtonDefaults.itemShape(1, 3),
                            modifier = Modifier.testTag("tab_register")
                        ) { Text("Đăng ký") }
                        SegmentedButton(
                            selected = activeTab == AuthTab.FORGOT,
                            onClick = { activeTab = AuthTab.FORGOT; errorMessage = null; infoMessage = null },
                            shape = SegmentedButtonDefaults.itemShape(2, 3),
                            modifier = Modifier.testTag("tab_forgot")
                        ) { Text("Quên MK") }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (!infoMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = infoMessage!!,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    FarmCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (activeTab == AuthTab.REGISTER) {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Họ và tên *") },
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    modifier = Modifier.fillMaxWidth().testTag("register_name_input"),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email *") },
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                                singleLine = true
                            )

                            if (activeTab == AuthTab.REGISTER || activeTab == AuthTab.FORGOT) {
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text(if (activeTab == AuthTab.FORGOT) "Số điện thoại xác minh *" else "Số điện thoại") },
                                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text(if (activeTab == AuthTab.FORGOT) "Mật khẩu mới (từ 6 ký tự) *" else "Mật khẩu *") },
                                leadingIcon = { Icon(Icons.Default.Lock, null) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Hiện/ẩn mật khẩu"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                                singleLine = true
                            )

                            if (activeTab == AuthTab.REGISTER) {
                                OutlinedTextField(
                                    value = farmName,
                                    onValueChange = { farmName = it },
                                    label = { Text("Tên trang trại") },
                                    leadingIcon = { Icon(Icons.Default.Agriculture, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Text("Vật nuôi chính của bạn:", style = MaterialTheme.typography.titleSmall)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    FarmConstants.SPECIES_LIST.forEach { sp ->
                                        FilterChip(
                                            selected = selectedSpecies.contains(sp),
                                            onClick = {
                                                selectedSpecies = if (selectedSpecies.contains(sp)) {
                                                    selectedSpecies - sp
                                                } else {
                                                    selectedSpecies + sp
                                                }
                                            },
                                            label = { Text(sp) }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    errorMessage = null
                                    infoMessage = null
                                    when (activeTab) {
                                        AuthTab.LOGIN -> viewModel.login(email, password) { errorMessage = it }
                                        AuthTab.REGISTER -> viewModel.register(
                                            fullName = fullName,
                                            email = email,
                                            phone = phone,
                                            password = password,
                                            farmName = farmName,
                                            speciesCsv = selectedSpecies.joinToString(",")
                                        ) { errorMessage = it }
                                        AuthTab.FORGOT -> viewModel.resetPassword(email, phone, password) { ok, msg ->
                                            if (ok) {
                                                infoMessage = msg
                                                activeTab = AuthTab.LOGIN
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_submit_button")
                            ) {
                                Text(
                                    text = when (activeTab) {
                                        AuthTab.LOGIN -> "Đăng nhập"
                                        AuthTab.REGISTER -> "Tạo tài khoản & bắt đầu"
                                        AuthTab.FORGOT -> "Đặt lại mật khẩu"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    // Clearly labeled Demo Accounts Card
                    FarmCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)) {
                        Text(
                            text = "Trải nghiệm nhanh với tài khoản mẫu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Dữ liệu mẫu có sẵn trang trại, đàn vật nuôi, chuồng trại, lịch chăm sóc và thu chi để bạn tham khảo ngay.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.login("nongdan@farmcare.vn", "123456") { errorMessage = it }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("demo_farmer_login")
                            ) {
                                Icon(Icons.Default.Agriculture, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Hộ chăn nuôi")
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.login("admin@farmcare.vn", "admin123") { errorMessage = it }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("demo_admin_login")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Quản trị viên")
                            }
                        }
                    }
                }
            }
        }
    }
}
