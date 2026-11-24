package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.FarmConstants
import com.example.data.repository.FarmFormatters
import com.example.ui.components.*
import com.example.ui.theme.FarmOnWarningContainer
import com.example.ui.theme.FarmWarningContainer
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun KnowledgeScreen(
    viewModel: FarmViewModel,
    onOpenArticle: (String) -> Unit
) {
    val articles by viewModel.publishedArticles.collectAsState()
    val newsList by viewModel.newsArticles.collectAsState()
    val marketPrices by viewModel.marketPrices.collectAsState()

    var topTab by remember { mutableIntStateOf(0) } // 0 = Kiến thức, 1 = Tin tức, 2 = Giá thị trường
    var query by remember { mutableStateOf("") }
    var speciesFilter by remember { mutableStateOf("Tất cả") }
    var categoryFilter by remember { mutableStateOf("Tất cả") }
    var libraryScope by remember { mutableStateOf("Tất cả") } // Tất cả | Đã lưu | Yêu thích | Đã đọc
    var regionFilter by remember { mutableStateOf("Tất cả") }

    Scaffold(
        topBar = { FarmTopBar("Tra cứu kiến thức & Thị trường") }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = topTab) {
                Tab(selected = topTab == 0, onClick = { topTab = 0 }, text = { Text("Kiến thức") })
                Tab(selected = topTab == 1, onClick = { topTab = 1 }, text = { Text("Tin tức") })
                Tab(selected = topTab == 2, onClick = { topTab = 2 }, text = { Text("Giá thị trường") })
            }

            when (topTab) {
                0 -> {
                    val filteredArticles = remember(articles, query, speciesFilter, categoryFilter, libraryScope) {
                        articles.filter { a ->
                            val matchQuery = query.isBlank() ||
                                a.title.contains(query, true) ||
                                a.keywords.contains(query, true) ||
                                a.summary.contains(query, true)
                            val matchSpecies = speciesFilter == "Tất cả" || a.species == speciesFilter || a.species == "Chung"
                            val matchCat = categoryFilter == "Tất cả" || a.category == categoryFilter
                            val matchScope = when (libraryScope) {
                                "Đã lưu" -> a.isBookmarked
                                "Yêu thích" -> a.isFavorite
                                "Đã đọc" -> a.lastReadAt != null
                                else -> true
                            }
                            matchQuery && matchSpecies && matchCat && matchScope
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Image(
                                painter = painterResource(id = R.drawable.img_knowledge_cover),
                                contentDescription = stringResource(R.string.cd_knowledge_cover),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        }
                        item {
                            FarmSearchField(query, { query = it }, "Tìm bệnh, kỹ thuật, giống, dinh dưỡng...")
                        }
                        item {
                            FilterChipRow(
                                options = listOf("Tất cả", "Đã lưu", "Yêu thích", "Đã đọc"),
                                selected = libraryScope,
                                onSelected = { libraryScope = it }
                            )
                        }
                        item {
                            FilterChipRow(
                                options = listOf("Tất cả") + FarmConstants.SPECIES_LIST,
                                selected = speciesFilter,
                                onSelected = { speciesFilter = it }
                            )
                        }
                        item {
                            FilterChipRow(
                                options = listOf("Tất cả") + FarmConstants.KNOWLEDGE_CATEGORIES,
                                selected = categoryFilter,
                                onSelected = { categoryFilter = it }
                            )
                        }
                        if (filteredArticles.isEmpty()) {
                            item {
                                EmptyStateView("Không tìm thấy bài viết", "Thử từ khóa khác hoặc đặt lại bộ lọc.", showIllustration = false)
                            }
                        } else {
                            items(filteredArticles, key = { it.id }) { article ->
                                FarmCard(onClick = { onOpenArticle(article.id) }) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            StatusBadge(article.category)
                                            StatusBadge(article.species)
                                        }
                                        Row {
                                            IconButton(onClick = { viewModel.toggleFavorite(article) }, modifier = Modifier.size(36.dp)) {
                                                Icon(
                                                    if (article.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Yêu thích",
                                                    tint = if (article.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(onClick = { viewModel.toggleBookmark(article) }, modifier = Modifier.size(36.dp)) {
                                                Icon(
                                                    if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "Lưu bài viết",
                                                    tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(article.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(6.dp))
                                    Text("${article.authorOrSource} • ${article.readMinutes} phút đọc", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val publishedNews = remember(newsList, speciesFilter, regionFilter) {
                        newsList.filter { it.isPublished }.filter {
                            (speciesFilter == "Tất cả" || it.species == speciesFilter) &&
                                (regionFilter == "Tất cả" || it.region == regionFilter || it.region == "Toàn quốc")
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(shape = MaterialTheme.shapes.small, color = FarmWarningContainer) {
                                Text(
                                    "Tin tức nội bộ do Quản trị viên cập nhật (không tự tạo tin giả lập thị trường thực tế).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FarmOnWarningContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        item {
                            FilterChipRow(listOf("Tất cả") + FarmConstants.SPECIES_LIST, speciesFilter) { speciesFilter = it }
                        }
                        if (publishedNews.isEmpty()) {
                            item { EmptyStateView("Chưa có tin tức phù hợp", "Quản trị viên có thể thêm tin tức mới trong Admin Dashboard.", showIllustration = false) }
                        } else {
                            items(publishedNews, key = { it.id }) { news ->
                                FarmCard {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        StatusBadge(news.category)
                                        Text(FarmFormatters.formatDate(news.publishedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(news.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(news.content, style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Nguồn: ${news.source} • Khu vực: ${news.region}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    val filteredPrices = remember(marketPrices, speciesFilter, regionFilter) {
                        marketPrices.filter { it.isPublished }.filter {
                            (speciesFilter == "Tất cả" || it.species == speciesFilter) &&
                                (regionFilter == "Tất cả" || it.region == regionFilter)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(shape = MaterialTheme.shapes.small, color = FarmWarningContainer) {
                                Text(
                                    "Bảng giá tham khảo do Quản trị viên cập nhật kèm nguồn và ngày cập nhật.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FarmOnWarningContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        item {
                            FilterChipRow(listOf("Tất cả") + FarmConstants.SPECIES_LIST, speciesFilter) { speciesFilter = it }
                        }
                        item {
                            FilterChipRow(listOf("Tất cả") + FarmConstants.REGIONS, regionFilter) { regionFilter = it }
                        }
                        items(filteredPrices, key = { it.id }) { mp ->
                            FarmCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(mp.productName, style = MaterialTheme.typography.titleMedium)
                                        Text("${mp.priceType} • ${mp.region}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "${FarmFormatters.formatVnd(mp.priceVnd)} / ${mp.unit.removePrefix("đ/")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Nguồn: ${mp.source} • Cập nhật: ${FarmFormatters.formatDate(mp.updatedAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleDetailScreen(
    articleId: String,
    viewModel: FarmViewModel,
    onBack: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    val context = LocalContext.current
    val article by viewModel.observeArticle(articleId).collectAsState(initial = null)
    val allArticles by viewModel.publishedArticles.collectAsState()

    LaunchedEffect(article?.id) {
        article?.let { viewModel.markArticleRead(it) }
    }

    val current = article
    if (current == null) {
        Scaffold(topBar = { FarmTopBar("Chi tiết bài viết", onBack = onBack) }) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy bài viết.")
            }
        }
        return
    }

    val related = remember(allArticles, current) {
        allArticles.filter { it.id != current.id && (it.species == current.species || it.category == current.category) }.take(3)
    }

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Bài viết kiến thức",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(current) }) {
                        Icon(if (current.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Yêu thích")
                    }
                    IconButton(onClick = { viewModel.toggleBookmark(current) }) {
                        Icon(if (current.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Lưu bài viết")
                    }
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, current.title)
                            putExtra(Intent.EXTRA_TEXT, "${current.title}\n\n${current.summary}\n\nNguồn: ${current.authorOrSource}")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ bài viết"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(current.category)
                StatusBadge(current.species)
            }
            Text(current.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "${current.authorOrSource} • Cập nhật ${FarmFormatters.formatDate(current.updatedAt)} • ${current.readMinutes} phút đọc",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(current.summary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

            FarmCard {
                current.content.lines().forEach { line ->
                    when {
                        line.startsWith("## ") -> {
                            Spacer(Modifier.height(6.dp))
                            Text(line.removePrefix("## "), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                        }
                        line.startsWith("- ") -> {
                            Text("• ${line.removePrefix("- ")}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
                        }
                        line.isNotBlank() -> {
                            Text(line, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                }
            }

            if (current.cautionNote.isNotBlank()) {
                Surface(shape = MaterialTheme.shapes.medium, color = FarmWarningContainer) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.WarningAmber, null, tint = FarmOnWarningContainer)
                        Column {
                            Text("Lưu ý quan trọng", style = MaterialTheme.typography.titleSmall, color = FarmOnWarningContainer)
                            Text(current.cautionNote, style = MaterialTheme.typography.bodySmall, color = FarmOnWarningContainer)
                        }
                    }
                }
            }

            if (related.isNotEmpty()) {
                Text("Bài viết liên quan", style = MaterialTheme.typography.titleMedium)
                related.forEach { rel ->
                    FarmCard(onClick = { onOpenArticle(rel.id) }) {
                        Text(rel.title, style = MaterialTheme.typography.titleSmall)
                        Text("${rel.category} • ${rel.species}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
