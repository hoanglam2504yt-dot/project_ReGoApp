package com.example.rego.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNotificationClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onMyAddressesClick: () -> Unit
) {
    val tealColor = Color(0xFF00897B)
    val context = LocalContext.current
    var isDarkMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Tài khoản",
                    style = MaterialTheme.typography.titleMedium,
                    color = tealColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(accountSettings) { item ->
                SettingItem(item) {
                    when (item.title) {
                        "Chỉnh sửa hồ sơ" -> onEditProfileClick()
                        "Đổi mật khẩu" -> onChangePasswordClick()
                        "Địa chỉ của tôi" -> onMyAddressesClick()
                        else -> Toast.makeText(context, "Chức năng ${item.title} đang được phát triển", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ứng dụng",
                    style = MaterialTheme.typography.titleMedium,
                    color = tealColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                // Special case for Dark Mode with Switch
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Chế độ tối", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            Text("Sử dụng giao diện Dark Mode", color = Color.Gray, fontSize = 13.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = tealColor, checkedTrackColor = tealColor.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            
            items(appSettings.filter { it.title != "Chế độ tối" }) { item ->
                SettingItem(item) {
                    when (item.title) {
                        "Thông báo" -> onNotificationClick()
                        else -> Toast.makeText(context, "Chức năng ${item.title} đang được phát triển", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất")
                }
            }
        }
    }
}

@Composable
fun SettingItem(item: SettingOption, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                if (item.subtitle.isNotEmpty()) {
                    Text(item.subtitle, color = Color.Gray, fontSize = 13.sp)
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

data class SettingOption(
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector
)

val accountSettings = listOf(
    SettingOption("Chỉnh sửa hồ sơ", "Thay đổi thông tin cá nhân, ảnh đại diện", Icons.Default.Edit),
    SettingOption("Đổi mật khẩu", "Cập nhật mật khẩu mới", Icons.Default.Lock),
    SettingOption("Địa chỉ của tôi", "Quản lý địa chỉ giao hàng", Icons.Default.LocationOn),
    SettingOption("Liên kết mạng xã hội", "Facebook, Google...", Icons.Default.Share)
)

val appSettings = listOf(
    SettingOption("Thông báo", "Cài đặt âm thanh, tin nhắn", Icons.Default.Notifications),
    SettingOption("Ngôn ngữ", "Tiếng Việt", Icons.Default.Language),
    SettingOption("Chế độ tối", "Sử dụng giao diện Dark Mode", Icons.Default.DarkMode),
    SettingOption("Trợ giúp & Hỗ trợ", "Trung tâm trợ giúp, phản hồi", Icons.Default.Help),
    SettingOption("Về ReGo", "Phiên bản 1.0.0", Icons.Default.Info)
)
