package com.example.rego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rego.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT, SEEN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    chatId: String,
    viewModel: ChatViewModel,
    currentUserId: String,
    onBackClick: () -> Unit
) {
    val receiverUser by viewModel.receiverUser.collectAsState()
    val chatPartnerName = receiverUser?.name ?: "Đang tải..."
    val chatPartnerAvatar = receiverUser?.avatarUrl ?: ""
    val lastActive = "Hoạt động gần đây"
    val tealColor = Color(0xFF00695C)
    
    var messageText by remember { mutableStateOf("") }
    
    // Khởi tạo chat room trong ViewModel
    LaunchedEffect(chatId, currentUserId) {
        viewModel.setupChat(currentUserId, chatId)
    }

    // Lấy tin nhắn từ ViewModel
    val chatMessages by viewModel.messages.collectAsState()
    
    // Map ChatMessage từ ViewModel sang Message của UI
    val messages = remember(chatMessages) {
        chatMessages.map { chatMsg ->
            Message(
                id = chatMsg.id,
                text = chatMsg.text,
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chatMsg.timestamp)),
                isFromMe = chatMsg.senderId == currentUserId,
                status = MessageStatus.SENT
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            AsyncImage(
                                model = chatPartnerAvatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(2.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(chatPartnerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(lastActive, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = tealColor)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Image, contentDescription = "Gallery", tint = tealColor)
                    }
                    
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("Nhập tin nhắn...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F6FA),
                            focusedContainerColor = Color(0xFFF5F6FA),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        maxLines = 4
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText, currentUserId, chatId)
                                messageText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = tealColor),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F6FA))
        ) {
            // Product info
            Card(
                modifier = Modifier.padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://example.com/product.jpg",
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Áo Thun Vintage 90s", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("250.000 đ", color = tealColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Mua ngay", fontSize = 13.sp)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Tin nhắn",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                item {
                    SecurityNotice()
                }

                items(messages) { message ->
                    MessageBubble(message, chatPartnerAvatar)
                }
            }
        }
    }
}

@Composable
fun SecurityNotice() {
    Surface(
        color = Color(0xFFEEEEEE),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Giao dịch an toàn: Vui lòng không chia sẻ thông tin thanh toán cá nhân ngoài nền tảng.",
                fontSize = 11.sp,
                color = Color.DarkGray,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MessageBubble(message: Message, partnerAvatar: String) {
    val tealColor = Color(0xFF00695C)
    
    val arrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isFromMe) {
            AsyncImage(
                model = partnerAvatar,
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = alignment
        ) {
            Surface(
                color = if (message.isFromMe) tealColor else Color.White,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (message.isFromMe) 12.dp else 0.dp,
                    bottomEnd = if (message.isFromMe) 0.dp else 12.dp
                ),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (message.isFromMe) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = message.time,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (message.isFromMe && message.status == MessageStatus.SEEN) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "Seen",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        if (message.isFromMe) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
