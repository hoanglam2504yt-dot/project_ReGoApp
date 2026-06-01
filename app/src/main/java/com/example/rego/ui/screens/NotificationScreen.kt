package com.example.rego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NotificationType {
    SHIPPING, PROMO, SECURITY, TRANSACTION
}

data class NotificationItem(
    val id: Int,
    val type: NotificationType,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit) {
    val notificationsToday = listOf(
        NotificationItem(
            1,
            NotificationType.SHIPPING,
            "Đơn hàng đang giao",
            "Người bán đã giao áo thun vintage cho đơn vị vận chuyển. Dự kiến giao hàng trong ngày mai.",
            "10:30 SA",
            isRead = false
        ),
        NotificationItem(
            2,
            NotificationType.PROMO,
            "Mã giảm giá 20% cho bạn!",
            "Khám phá các sản phẩm thời trang tái chế với ưu đãi đặc biệt hôm nay.",
            "08:15 SA",
            isRead = true
        )
    )

    val notificationsBefore = listOf(
        NotificationItem(
            3,
            NotificationType.SECURITY,
            "Đăng nhập từ thiết bị mới",
            "Tài khoản của bạn vừa được đăng nhập trên một thiết bị lạ. Vui lòng kiểm tra nếu không phải bạn.",
            "Hôm qua, 14:20",
            isRead = true
        ),
        NotificationItem(
            4,
            NotificationType.TRANSACTION,
            "Giao dịch thành công",
            "Đơn hàng #CM-8472 của bạn đã hoàn thành. Hãy đánh giá người bán nhé!",
            "12 thg 10",
            isRead = true
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Thông báo", 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF005D4B),
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color(0xFF005D4B)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                SectionHeader("Hôm nay")
            }
            items(notificationsToday) { item ->
                NotificationCard(item)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Trước đó")
            }
            items(notificationsBefore) { item ->
                NotificationCard(item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun NotificationCard(item: NotificationItem) {
    val (icon, iconBgColor) = when (item.type) {
        NotificationType.SHIPPING -> Icons.Default.LocalShipping to Color(0xFF005D4B)
        NotificationType.PROMO -> Icons.Default.Redeem to Color(0xFFF57C00)
        NotificationType.SECURITY -> Icons.Default.Shield to Color(0xFFD32F2F).copy(alpha = 0.1f)
        NotificationType.TRANSACTION -> Icons.Default.CheckCircle to Color(0xFF607D8B)
    }
    
    val iconTint = if (item.type == NotificationType.SECURITY) Color(0xFFD32F2F) else Color.White
    val cardBg = if (item.isRead) Color.White else Color(0xFFF1FBF9)
    val borderColor = if (item.isRead) Color(0xFFEEEEEE) else Color(0xFFE0F2F1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.title, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF005D4B))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    item.message, 
                    fontSize = 13.sp, 
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    item.time, 
                    fontSize = 12.sp, 
                    color = Color.Gray
                )
            }
        }
    }
}
