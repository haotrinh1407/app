package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.FarmFormatters
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FarmCard
import com.example.ui.components.FarmTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.FarmViewModel

@Composable
fun NotificationsScreen(
    viewModel: FarmViewModel,
    onBack: () -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Trung tâm thông báo",
                onBack = onBack,
                actions = {
                    if (notifications.any { !it.isRead }) {
                        IconButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Đọc tất cả")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    title = "Không có thông báo mới",
                    subtitle = "Các nhắc nhở tiêm phòng, tẩy giun, sinh sản và công việc quá hạn sẽ xuất hiện tại đây.",
                    showIllustration = false
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    FarmCard(
                        containerColor = if (notif.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            if (notif.targetRoute.isNotBlank()) {
                                onNavigateRoute(notif.targetRoute)
                            }
                        }
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(notif.category)
                            IconButton(onClick = { viewModel.deleteNotification(notif.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa thông báo", modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            notif.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold
                        )
                        Text(notif.message, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(FarmFormatters.formatDateTime(notif.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
