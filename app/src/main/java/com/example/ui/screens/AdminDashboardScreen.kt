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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.KnowledgeArticleEntity
import com.example.data.local.MarketPriceEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    val farms by viewModel.allFarms.collectAsState()
    val articles by viewModel.allArticles.collectAsState()
    val news by viewModel.newsArticles.collectAsState()
    val prices by viewModel.marketPrices.collectAsState()
    val feedbacks by viewModel.allFeedback.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 Tổng quan, 1 Bài viết, 2 Tin tức, 3 Giá, 4 Người dùng
    var query by remember { mutableStateOf("") }

    var editingArticle by remember { mutableStateOf<KnowledgeArticleEntity?>(null) }
    var showArticleDialog by remember { mutableStateOf(false) }
    var showNewsDialog by remember { mutableStateOf(false) }
    var showPriceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { FarmTopBar("Admin Dashboard", onBack = onBack) },
        floatingActionButton = {
            if (selectedTab in 1..3) {
                FloatingActionButton(onClick = {
                    when (selectedTab) {
                        1 -> { editingArticle = null; showArticleDialog = true }
                        2 -> showNewsDialog = true
                        3 -> showPriceDialog = true
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm nội dung")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tổng quan") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Kiến thức (${articles.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Tin tức (${news.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Thị trường (${prices.size})") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Người dùng (${users.size})") })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 84.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab in 1..4) {
                    item {
                        FarmSearchField(query, { query = it }, "Tìm kiếm nhanh trong mục quản trị...")
                    }
                }

                when (selectedTab) {
                    0 -> {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatMetricTile("Người dùng", users.size.toString(), Icons.Default.People, modifier = Modifier.weight(1f))
                                StatMetricTile("Trang trại", farms.size.toString(), Icons.Default.Agriculture, modifier = Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatMetricTile("Bài kiến thức", articles.size.toString(), Icons.Default.MenuBook, modifier = Modifier.weight(1f))
                                StatMetricTile("Phản hồi", feedbacks.size.toString(), Icons.Default.Feedback, modifier = Modifier.weight(1f))
                            }
                        }
                        item { Text("Danh sách trang trại hệ thống", style = MaterialTheme.typography.titleMedium) }
                        items(farms, key = { it.id }) { f ->
                            FarmCard {
                                Text(f.name, style = MaterialTheme.typography.titleSmall)
                                Text("${f.farmingType} • ${f.scale} • ${f.address.ifBlank { "Chưa rõ địa chỉ" }}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (feedbacks.isNotEmpty()) {
                            item { Text("Phản hồi & Báo lỗi từ người dùng", style = MaterialTheme.typography.titleMedium) }
                            items(feedbacks, key = { it.id }) { fb ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        StatusBadge(fb.feedbackType)
                                        Text(FarmFormatters.formatDate(fb.createdAt), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(fb.subject, style = MaterialTheme.typography.titleSmall)
                                    Text(fb.message, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    1 -> {
                        val filtered = articles.filter { query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true) }
                        items(filtered, key = { it.id }) { art ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(if (art.isPublished) "Hiển thị" else "Đang ẩn")
                                    StatusBadge(art.species)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(art.title, style = MaterialTheme.typography.titleSmall)
                                Text("${art.category} • ${art.authorOrSource}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { viewModel.adminSaveArticle(art.copy(isPublished = !art.isPublished)) }) {
                                        Text(if (art.isPublished) "Ẩn" else "Hiện")
                                    }
                                    TextButton(onClick = { editingArticle = art; showArticleDialog = true }) { Text("Sửa") }
                                    TextButton(onClick = { viewModel.adminDeleteArticle(art.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }

                    2 -> {
                        val filtered = news.filter { query.isBlank() || it.title.contains(query, true) }
                        items(filtered, key = { it.id }) { item ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    StatusBadge(item.category)
                                    StatusBadge(if (item.isPublished) "Hiển thị" else "Đang ẩn")
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.title, style = MaterialTheme.typography.titleSmall)
                                Text("${item.source} • ${item.region}", style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { viewModel.adminSaveNews(item.copy(isPublished = !item.isPublished)) }) {
                                        Text(if (item.isPublished) "Ẩn" else "Hiện")
                                    }
                                    TextButton(onClick = { viewModel.adminDeleteNews(item.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }

                    3 -> {
                        val filtered = prices.filter { query.isBlank() || it.productName.contains(query, true) }
                        items(filtered, key = { it.id }) { mp ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(mp.productName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Text(FarmFormatters.formatVnd(mp.priceVnd), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("${mp.priceType} • ${mp.species} • ${mp.region}", style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { viewModel.adminDeleteMarketPrice(mp.id) }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }

                    4 -> {
                        val filtered = users.filter { query.isBlank() || it.fullName.contains(query, true) || it.email.contains(query, true) }
                        items(filtered, key = { it.id }) { u ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(u.fullName, style = MaterialTheme.typography.titleSmall)
                                        Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(u.role)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {
                                        val nextRole = if (u.role == "ADMIN") "USER" else "ADMIN"
                                        viewModel.adminUpdateUser(u.id, nextRole, u.isActive)
                                    }) {
                                        Text(if (u.role == "ADMIN") "Chuyển User" else "Nâng Admin")
                                    }
                                    TextButton(onClick = {
                                        viewModel.adminUpdateUser(u.id, u.role, !u.isActive)
                                    }) {
                                        Text(if (u.isActive) "Khóa" else "Mở khóa")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showArticleDialog) {
        var title by remember { mutableStateOf(editingArticle?.title ?: "") }
        var category by remember { mutableStateOf(editingArticle?.category ?: FarmConstants.KNOWLEDGE_CATEGORIES.first()) }
        var species by remember { mutableStateOf(editingArticle?.species ?: "Dê") }
        var summary by remember { mutableStateOf(editingArticle?.summary ?: "") }
        var content by remember { mutableStateOf(editingArticle?.content ?: "") }
        var source by remember { mutableStateOf(editingArticle?.authorOrSource ?: "Ban biên tập FarmCare") }
        AlertDialog(
            onDismissRequest = { showArticleDialog = false },
            title = { Text(if (editingArticle == null) "Thêm bài viết kiến thức" else "Sửa bài viết") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tiêu đề *") })
                    SimpleDropdownSelector("Danh mục", FarmConstants.KNOWLEDGE_CATEGORIES, category) { category = it }
                    SimpleDropdownSelector("Loài", listOf("Chung") + FarmConstants.SPECIES_LIST, species) { species = it }
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Tóm tắt *") })
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Nội dung *") }, minLines = 3)
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Nguồn tham khảo") })
                }
            },
            confirmButton = {
                Button(enabled = title.isNotBlank() && content.isNotBlank(), onClick = {
                    viewModel.adminSaveArticle(
                        (editingArticle ?: KnowledgeArticleEntity(
                            id = "",
                            title = title.trim(),
                            category = category,
                            species = species,
                            summary = summary.trim(),
                            content = content.trim(),
                            authorOrSource = source.trim()
                        )).copy(
                            title = title.trim(),
                            category = category,
                            species = species,
                            summary = summary.trim(),
                            content = content.trim(),
                            authorOrSource = source.trim()
                        )
                    )
                    showArticleDialog = false
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showArticleDialog = false }) { Text("Hủy") } }
        )
    }

    if (showNewsDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(FarmConstants.NEWS_CATEGORIES.first()) }
        var species by remember { mutableStateOf("Bò") }
        var region by remember { mutableStateOf("Toàn quốc") }
        var content by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("Quản trị viên FarmCare") }
        AlertDialog(
            onDismissRequest = { showNewsDialog = false },
            title = { Text("Thêm bản tin mới") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tiêu đề *") })
                    SimpleDropdownSelector("Chuyên mục", FarmConstants.NEWS_CATEGORIES, category) { category = it }
                    SimpleDropdownSelector("Loài", FarmConstants.SPECIES_LIST, species) { species = it }
                    SimpleDropdownSelector("Khu vực", FarmConstants.REGIONS, region) { region = it }
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Nội dung *") }, minLines = 3)
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Nguồn") })
                }
            },
            confirmButton = {
                Button(enabled = title.isNotBlank() && content.isNotBlank(), onClick = {
                    viewModel.adminSaveNews(
                        NewsArticleEntity(
                            id = "",
                            title = title.trim(),
                            category = category,
                            species = species,
                            region = region,
                            summary = content.trim().take(120),
                            content = content.trim(),
                            source = source.trim(),
                            publishedAt = System.currentTimeMillis()
                        )
                    )
                    showNewsDialog = false
                }) { Text("Đăng tin") }
            },
            dismissButton = { TextButton(onClick = { showNewsDialog = false }) { Text("Hủy") } }
        )
    }

    if (showPriceDialog) {
        var name by remember { mutableStateOf("") }
        var priceType by remember { mutableStateOf(FarmConstants.MARKET_PRICE_TYPES.first()) }
        var species by remember { mutableStateOf("Dê") }
        var region by remember { mutableStateOf("Miền Nam") }
        var priceText by remember { mutableStateOf("100000") }
        var unit by remember { mutableStateOf("đ/kg") }
        var source by remember { mutableStateOf("Quản trị viên cập nhật") }
        AlertDialog(
            onDismissRequest = { showPriceDialog = false },
            title = { Text("Cập nhật giá thị trường") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm / vật nuôi *") })
                    SimpleDropdownSelector("Loại giá", FarmConstants.MARKET_PRICE_TYPES, priceType) { priceType = it }
                    SimpleDropdownSelector("Loài", FarmConstants.SPECIES_LIST, species) { species = it }
                    SimpleDropdownSelector("Khu vực", FarmConstants.REGIONS, region) { region = it }
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Đơn giá (VNĐ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Đơn vị") })
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Nguồn") })
                }
            },
            confirmButton = {
                Button(enabled = name.isNotBlank(), onClick = {
                    viewModel.adminSaveMarketPrice(
                        MarketPriceEntity(
                            id = "",
                            productName = name.trim(),
                            priceType = priceType,
                            species = species,
                            region = region,
                            priceVnd = priceText.toLongOrNull() ?: 0L,
                            unit = unit.trim(),
                            source = source.trim(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    showPriceDialog = false
                }) { Text("Lưu giá") }
            },
            dismissButton = { TextButton(onClick = { showPriceDialog = false }) { Text("Hủy") } }
        )
    }
}
