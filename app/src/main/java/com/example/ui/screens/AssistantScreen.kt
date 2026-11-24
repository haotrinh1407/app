package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.viewmodel.FarmViewModel

private val SUGGESTED_QUESTIONS = listOf(
    "Cách làm chuồng sàn nuôi dê thoáng mát?",
    "Lịch tiêm phòng cho bò và gà con?",
    "Cách phối trộn thức ăn cho bò vỗ béo?",
    "Dấu hiệu heo bị bệnh cần theo dõi?",
    "Chăm sóc dê mang thai sắp sinh?"
)

@Composable
fun AssistantScreen(
    viewModel: FarmViewModel,
    onOpenArticle: (String) -> Unit
) {
    val sessions by viewModel.chatSessions.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val articles by viewModel.publishedArticles.collectAsState()
    val isReplying by viewModel.isAiReplying.collectAsState()

    var input by remember { mutableStateOf("") }
    var showSessionsSheet by remember { mutableStateOf(false) }

    val articleMap = remember(articles) { articles.associateBy { it.id } }

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Trợ lý chăn nuôi AI",
                actions = {
                    IconButton(onClick = { showSessionsSheet = true }) {
                        Icon(Icons.Default.History, contentDescription = "Lịch sử trò chuyện")
                    }
                    IconButton(onClick = { viewModel.startNewChatSession() }) {
                        Icon(Icons.Default.AddComment, contentDescription = "Cuộc trò chuyện mới")
                    }
                    IconButton(onClick = { viewModel.clearActiveChat() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Xóa lịch sử")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiFirebaseNoticeBanner()

            // Suggestion Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SUGGESTED_QUESTIONS.forEach { q ->
                    AssistChip(
                        onClick = { viewModel.sendAiQuestion(q) },
                        label = { Text(q, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "Hỏi đáp kiến thức chăn nuôi",
                            subtitle = "Chọn một câu hỏi gợi ý phía trên hoặc nhập thắc mắc về dê, bò, heo, gà, chuồng trại, dinh dưỡng...",
                            showIllustration = false
                        )
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.role == "USER"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 330.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isUser) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!isUser && msg.referenceSource.isNotBlank()) {
                                        Text(
                                            text = "Nguồn: ${msg.referenceSource}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    val relIds = msg.relatedArticleIdsCsv.split(",").filter { it.isNotBlank() }
                                    if (!isUser && relIds.isNotEmpty()) {
                                        relIds.mapNotNull { articleMap[it] }.forEach { art ->
                                            OutlinedButton(
                                                onClick = { onOpenArticle(art.id) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.heightIn(min = 38.dp)
                                            ) {
                                                Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(art.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isReplying) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Đang tổng hợp từ thư viện kiến thức...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Nhập câu hỏi chăn nuôi...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    maxLines = 3,
                    shape = MaterialTheme.shapes.medium
                )
                FilledIconButton(
                    onClick = {
                        val q = input.trim()
                        if (q.isNotBlank()) {
                            input = ""
                            viewModel.sendAiQuestion(q)
                        }
                    },
                    enabled = input.isNotBlank() && !isReplying,
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("ai_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi câu hỏi")
                }
            }
        }
    }

    if (showSessionsSheet) {
        AlertDialog(
            onDismissRequest = { showSessionsSheet = false },
            title = { Text("Lịch sử phiên hỏi đáp") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sessions.isEmpty()) {
                        Text("Chưa có phiên trò chuyện nào.")
                    } else {
                        sessions.forEach { s ->
                            Surface(
                                onClick = {
                                    viewModel.selectChatSession(s.id)
                                    showSessionsSheet = false
                                },
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(s.title, modifier = Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSessionsSheet = false }) { Text("Đóng") } }
        )
    }
}
