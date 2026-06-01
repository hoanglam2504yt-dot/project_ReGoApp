package com.example.rego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rego.data.local.entities.Product
import com.example.rego.ui.components.MarketProductItem
import com.example.rego.ui.components.FilterBottomSheet
import com.example.rego.ui.viewmodel.HomeViewModel
import com.example.rego.ui.viewmodel.CartViewModel
import com.example.rego.ui.viewmodel.ProfileViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    profileViewModel: ProfileViewModel,
    isLoggedIn: Boolean,
    userId: Int,
    onProductClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showFilterSheet) {
        FilterBottomSheet(
            initialMinPrice = minPrice,
            initialMaxPrice = maxPrice,
            initialSortOption = selectedSort,
            onApply = { min, max, sort ->
                viewModel.applyFilters(min, max, sort)
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoggedIn) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { onProfileClick() },
                                shape = CircleShape,
                                color = Color(0xFF00897B)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.padding(4.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            "ReGo",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF00897B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                        }
                    } else {
                        TextButton(onClick = onLoginClick) {
                            Text("Đăng nhập/Đăng ký", color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 1. Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm mặt hàng, thương hiệu...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF00897B)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Categories Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Xem tất cả", 
                    color = Color(0xFF00897B), 
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(categories) { category ->
                    CategoryCircleItem(
                        name = category.name,
                        iconUrl = category.iconUrl,
                        onClick = { viewModel.onCategorySelected(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Latest Posts Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tin đăng mới nhất", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showFilterSheet = true }
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF00897B))
                    Text(" Bộ lọc", color = Color(0xFF00897B), fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            products.chunked(2).forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { product ->
                        Box(modifier = Modifier.weight(1f)) {
                            MarketProductItem(
                                product = product,
                                onClick = { onProductClick(product.id.toString()) },
                                onAddToCart = {
                                    if (isLoggedIn) {
                                        cartViewModel.addToCart(product.id)
                                    } else {
                                        onLoginClick()
                                    }
                                },
                                profileViewModel = profileViewModel,
                                userId = userId
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Featured Sellers
            Text("Người bán nổi bật", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { // Mock 3 sellers
                    SellerCard()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CategoryCircleItem(name: String, iconUrl: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0F2F1)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = iconUrl,
                contentDescription = name,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SellerCard() {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.LightGray) {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Sarah J.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Text(" 4.9 (124)", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(4.dp)) {
                    Text("Thành viên 3 năm", modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                }
                Surface(color = Color(0xFFE0F2F1), shape = RoundedCornerShape(4.dp)) {
                    Text("Phản hồi nhanh", modifier = Modifier.padding(4.dp), fontSize = 10.sp, color = Color(0xFF00897B))
                }
            }
        }
    }
}
