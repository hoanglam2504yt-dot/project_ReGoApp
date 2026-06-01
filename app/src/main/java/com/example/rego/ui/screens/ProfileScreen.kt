package com.example.rego.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rego.data.local.entities.User
import com.example.rego.ui.components.MarketProductItem
import com.example.rego.ui.viewmodel.*
import kotlinx.coroutines.Job
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    userId: Int,
    isMine: Boolean = true,
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProductClick: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Job
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // States for Edit Profile
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val userProducts by viewModel.getProductsBySeller(userId).collectAsState(initial = emptyList())
    val favoriteProducts by viewModel.getFavoriteProducts(userId).collectAsState(initial = emptyList())
    val userOrders by viewModel.getOrders(userId).collectAsState(initial = emptyList())
    val user by viewModel.getUser(userId).collectAsState(initial = null)
    val authState by authViewModel.authState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    
    val tealColor = Color(0xFF00796B)
    val backgroundColor = Color(0xFFF5F5F5)

    // Image Picker Launcher
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    LaunchedEffect(user, isEditingProfile) {
        if (isEditingProfile && user != null) {
            editName = user?.name ?: ""
            editPhone = user?.phone ?: ""
            editAddress = user?.address ?: ""
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is ProfileUpdateState.Success) {
            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            isEditingProfile = false
            viewModel.resetUpdateState()
        } else if (updateState is ProfileUpdateState.Error) {
            Toast.makeText(context, (updateState as ProfileUpdateState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetUpdateState()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedOut) {
            onLogout()
            authViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isMine && !isEditingProfile) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(2) }) {
                    if (isEditingProfile) {
                        EditProfileCard(
                            name = editName,
                            onNameChange = { editName = it },
                            phone = editPhone,
                            onPhoneChange = { editPhone = it },
                            address = editAddress,
                            onAddressChange = { editAddress = it },
                            avatarUrl = user?.avatarUrl,
                            selectedImageUri = selectedImageUri,
                            onPickImage = { photoLauncher.launch("image/*") },
                            onSave = {
                                if (editPhone.length == 10 && editPhone.all { it.isDigit() }) {
                                    user?.let {
                                        viewModel.updateProfile(
                                            it.copy(name = editName, phone = editPhone, address = editAddress),
                                            selectedImageUri
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "Số điện thoại phải có 10 chữ số", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCancel = { isEditingProfile = false },
                            isLoading = updateState is ProfileUpdateState.Loading,
                            tealColor = tealColor
                        )
                    } else {
                        ProfileHeader(
                            user = user,
                            tealColor = tealColor, 
                            isMine = isMine,
                            onEditClick = { isEditingProfile = true }
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    ProfileStats(userProducts.size.toString())
                }

                item(span = { GridItemSpan(2) }) {
                    if (isMine) {
                        ProfileTabs(selectedTab, onTabSelected = { selectedTab = it }, tealColor)
                    } else {
                        Text("Tin đã đăng", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Content logic
                when {
                    selectedTab == 2 && isMine -> {
                        if (userOrders.isEmpty()) {
                            item(span = { GridItemSpan(2) }) { EmptyStateView("Bạn chưa có đơn hàng nào", Icons.Default.ShoppingCart) }
                        } else {
                            items(userOrders, span = { GridItemSpan(2) }) { OrderItemCard(it, tealColor) }
                        }
                    }
                    else -> {
                        val displayProducts = if (isMine) {
                            if (selectedTab == 0) userProducts else favoriteProducts
                        } else userProducts

                        if (displayProducts.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                val msg = if (selectedTab == 0) "Bạn chưa đăng tin nào" else "Chưa có tin yêu thích"
                                EmptyStateView(msg, if (selectedTab == 0) Icons.Default.PostAdd else Icons.Default.FavoriteBorder)
                            }
                        } else {
                            items(displayProducts) { product ->
                                Box {
                                    MarketProductItem(
                                        product = product,
                                        onClick = { onProductClick(product.id.toString()) },
                                        onAddToCart = { },
                                        profileViewModel = viewModel,
                                        userId = userId
                                    )
                                    if (isMine && selectedTab == 0) {
                                        IconButton(
                                            onClick = { viewModel.deleteProduct(product.id) },
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).size(30.dp)
                                                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileCard(
    name: String, onNameChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    avatarUrl: String?,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    tealColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp).clickable { onPickImage() }) {
                AsyncImage(
                    model = selectedImageUri ?: avatarUrl ?: "https://i.pravatar.cc/150",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, tealColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Surface(color = tealColor, shape = CircleShape, modifier = Modifier.size(24.dp).align(Alignment.BottomEnd)) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Họ tên") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = onAddressChange, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Hủy") }
                Button(onClick = onSave, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = tealColor), enabled = !isLoading) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Lưu")
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileHeader(user: User?, tealColor: Color, isMine: Boolean, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user?.avatarUrl ?: "https://i.pravatar.cc/150?u=${user?.id}",
                contentDescription = "Avatar",
                modifier = Modifier.size(70.dp).clip(CircleShape).border(1.dp, Color.LightGray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user?.name ?: "Người dùng ReGo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(user?.phone ?: "Chưa cập nhật SĐT", fontSize = 13.sp, color = Color.Gray)
                if (isMine) {
                    TextButton(onClick = onEditClick, contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(14.dp), tint = tealColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chỉnh sửa thông tin", fontSize = 12.sp, color = tealColor)
                    }
                }
            }
            if (user?.id == 1) Icon(Icons.Default.Verified, null, tint = tealColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ProfileStats(postCount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(postCount, "Tin đăng")
        StatItem("120", "Người theo dõi")
        StatItem("100%", "Phản hồi")
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileTabs(selectedTab: Int, onTabSelected: (Int) -> Unit, tealColor: Color) {
    val tabs = listOf("Tin đăng", "Yêu thích", "Đơn hàng")
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEEEEE)).padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onTabSelected(index) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.Black else Color.Gray)
            }
        }
    }
}

@Composable
fun OrderItemCard(orderWithProducts: OrderWithProducts, tealColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đơn hàng #${orderWithProducts.order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    orderWithProducts.order.status,
                    color = tealColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.background(tealColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            orderWithProducts.products.forEach { product ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text("${String.format(Locale("vi", "VN"), "%,.0f", product.price)} đ", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Tổng tiền: ", fontSize = 14.sp)
                Text("${String.format(Locale("vi", "VN"), "%,.0f", orderWithProducts.order.totalAmount)} đ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B4513))
            }
        }
    }
}
