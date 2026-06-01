package com.example.rego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rego.ui.viewmodel.ProductDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String?,
    viewModel: ProductDetailViewModel,
    currentUserId: Int,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onSellerClick: (Int) -> Unit,
    onBuyNowClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    val product by viewModel.product.collectAsState()
    val seller by viewModel.seller.collectAsState()
    val scrollState = rememberScrollState()
    
    val tealColor = Color(0xFF00796B)
    val isFavorite by (if (product != null) viewModel.isFavorite(currentUserId, product!!.id).collectAsState(initial = false) else remember { mutableStateOf(false) })

    val isMyProduct = product?.sellerId == currentUserId
    val isSold = product?.isSold ?: false

    var showSoldConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        productId?.toIntOrNull()?.let { viewModel.loadProduct(it) }
    }

    if (showSoldConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSoldConfirmDialog = false },
            title = { Text("Xác nhận đã bán", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có muốn xác nhận đơn hàng này đã bán hay không? Sau khi xác nhận, người mua sẽ không thể thực hiện mua hàng.") },
            confirmButton = {
                Button(
                    onClick = {
                        product?.let { viewModel.markAsSold(it.id) }
                        showSoldConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSoldConfirmDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm", color = tealColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = tealColor)
                    }
                },
                actions = {
                    if (!isMyProduct && !isSold) {
                        IconButton(onClick = { 
                            product?.let { viewModel.toggleFavorite(currentUserId, it.id, isFavorite) }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMyProduct) {
                        OutlinedButton(
                            onClick = { product?.let { onEditClick(it.id.toString()) } },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tealColor)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = tealColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chỉnh sửa", fontWeight = FontWeight.Bold, color = tealColor, fontSize = 14.sp)
                        }
                        
                        if (!isSold) {
                            Button(
                                onClick = { showSoldConfirmDialog = true },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đã bán", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        } else {
                            Surface(modifier = Modifier.weight(1f).height(50.dp), color = Color(0xFFF1F3F4), shape = RoundedCornerShape(12.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Sản phẩm đã bán", color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onChatClick(product?.sellerId.toString()) },
                            modifier = Modifier.weight(if (isSold) 1f else 2f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tealColor)
                        ) {
                            Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(18.dp), tint = tealColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat ngay", color = tealColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        
                        if (!isSold) {
                            Button(
                                onClick = { product?.let { onBuyNowClick(it.id.toString()) } },
                                modifier = Modifier.weight(1.2f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mua Ngay", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        product?.let { p ->
            val images = listOfNotNull(p.imageUrl, p.imageUrl2, p.imageUrl3).filter { it.isNotBlank() }
            var currentImageIndex by remember { mutableIntStateOf(0) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8F9FA))
                    .verticalScroll(scrollState)
            ) {
                // IMAGE SLIDER SECTION
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    if (images.isNotEmpty()) {
                        AsyncImage(
                            model = images[currentImageIndex],
                            contentDescription = p.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (images.size > 1) {
                            IconButton(
                                onClick = { if (currentImageIndex > 0) currentImageIndex-- else currentImageIndex = images.size - 1 },
                                modifier = Modifier.align(Alignment.CenterStart).padding(8.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.ChevronLeft, "Prev", tint = Color.White)
                            }

                            IconButton(
                                onClick = { if (currentImageIndex < images.size - 1) currentImageIndex++ else currentImageIndex = 0 },
                                modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.ChevronRight, "Next", tint = Color.White)
                            }
                        }

                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "${currentImageIndex + 1}/${images.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    if (isSold) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp)) {
                                Text("ĐÃ BÁN", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.Red)
                            }
                        }
                    } else if (p.isVerified) {
                        Surface(modifier = Modifier.padding(16.dp).align(Alignment.TopStart), color = tealColor, shape = RoundedCornerShape(4.dp)) {
                            Text("Đã Xác Minh", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${String.format(Locale("vi", "VN"), "%,.0f", p.price)} đ", style = MaterialTheme.typography.titleLarge, color = if (isSold) Color.Gray else tealColor, fontWeight = FontWeight.Bold)
                    }
                    
                    Text("Miễn phí vận chuyển", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgeItem(icon = Icons.Default.CheckCircle, label = p.condition)
                        BadgeItem(icon = Icons.Default.Category, label = "Sản phẩm")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(p.shippingInfo, color = Color.Gray, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mô tả Sản phẩm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(p.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, lineHeight = 22.sp)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                SpecInfo(label = "THƯƠNG HIỆU", value = p.brand, modifier = Modifier.weight(1f))
                                SpecInfo(label = "DÒNG MÁY", value = p.model, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isMyProduct) {
                        Surface(modifier = Modifier.fillMaxWidth().clickable { onSellerClick(p.sellerId) }, color = Color.White, shape = RoundedCornerShape(16.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = "https://i.pravatar.cc/150?u=${p.sellerId}", contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(seller?.name ?: "Người bán", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Thành viên ReGo", fontSize = 12.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Địa điểm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = tealColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(p.location, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = tealColor)
        }
    }
}

@Composable
fun BadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(color = Color(0xFFF1F3F4), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color(0xFF00796B))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SpecInfo(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value.ifBlank { "Không có" }, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}
