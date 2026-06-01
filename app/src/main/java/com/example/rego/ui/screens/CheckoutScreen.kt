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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rego.ui.components.SellDropdownSelection
import com.example.rego.ui.viewmodel.CheckoutViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onChangeAddressClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val tealColor = Color(0xFF00796B)
    val backgroundColor = Color(0xFFF8F9FA)
    
    val addressSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDiscountSheet by remember { mutableStateOf(false) }
    val discountSheetState = rememberModalBottomSheetState()

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, color = Color.White) {
                Button(
                    onClick = onPaymentSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Xác nhận thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = tealColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Delivery Address Block
                CheckoutCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ĐỊA CHỈ NHẬN HÀNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Text(
                                "Thay đổi", 
                                color = tealColor, 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onChangeAddressClick() }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "${uiState.recipientName} • ${uiState.recipientPhone}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            uiState.shippingAddress,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFE0F2F1),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = tealColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MẶC ĐỊNH", color = tealColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. Order Block
                CheckoutCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ĐƠN HÀNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        uiState.products.forEach { (product, quantity) ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.title, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 2)
                                    Text("Phân loại: Mặc định", fontSize = 12.sp, color = Color.Gray)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${String.format(Locale("vi", "VN"), "%,.0f", product.price)} đ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = tealColor
                                        )
                                        Text("x$quantity", color = Color.Gray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Seller Block
                CheckoutCard(containerColor = tealColor) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://i.pravatar.cc/150?u=${uiState.seller?.id ?: 0}",
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(uiState.seller?.name ?: "Người bán ReGo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(14.dp))
                                Text(" 4.9 (1.2k)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 4. Shipping Method Block
                CheckoutCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PHƯƠNG THỨC VẬN CHUYỂN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Option 1: Instant
                        ShippingOption(
                            title = "Giao hàng hỏa tốc",
                            subtitle = "Nhận hàng trong 2 giờ",
                            fee = 35000.0,
                            icon = Icons.Default.ElectricBolt,
                            isSelected = uiState.shippingMethod == "Giao hàng hỏa tốc",
                            tealColor = tealColor,
                            onClick = { viewModel.setShippingMethod("Giao hàng hỏa tốc", 35000.0) }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Option 2: Standard
                        ShippingOption(
                            title = "Giao hàng tiêu chuẩn",
                            subtitle = "Nhận hàng sau 3-4 ngày",
                            fee = 15000.0,
                            icon = Icons.Default.LocalShipping,
                            isSelected = uiState.shippingMethod == "Giao hàng tiêu chuẩn",
                            tealColor = tealColor,
                            onClick = { viewModel.setShippingMethod("Giao hàng tiêu chuẩn", 15000.0) }
                        )
                    }
                }

                // 5. Promo Code Block
                CheckoutCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDiscountSheet = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ReGo Voucher", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Chọn hoặc nhập mã", color = Color.Gray, fontSize = 13.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }

                // 6. Payment Method Block
                CheckoutCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Payments, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PHƯƠNG THỨC THANH TOÁN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        PaymentOption("Ví MoMo", "https://upload.wikimedia.org/wikipedia/vi/f/fe/MoMo_Logo.png", uiState.paymentMethod == "Ví MoMo") { viewModel.setPaymentMethod("Ví MoMo") }
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentOption("Thẻ ngân hàng (Visa/Master)", null, uiState.paymentMethod == "Thẻ ngân hàng (Visa/Master)", icon = Icons.Default.CreditCard) { viewModel.setPaymentMethod("Thẻ ngân hàng (Visa/Master)") }
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentOption("Thanh toán khi nhận hàng (COD)", null, uiState.paymentMethod == "COD", icon = Icons.Default.Payments) { viewModel.setPaymentMethod("COD") }
                    }
                }

                // 7. Summary Block
                CheckoutCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val subtotal = uiState.products.sumOf { it.first.price * it.second }
                        val shippingFee = uiState.shippingFee
                        val shippingDiscount = uiState.discount 
                        val voucherDiscount = 0.0
                        val total = subtotal + shippingFee - shippingDiscount - voucherDiscount
                        
                        SummaryRow("Tổng tiền hàng", "${String.format(Locale("vi", "VN"), "%,.0f", subtotal)} đ")
                        SummaryRow("Phí vận chuyển", "${String.format(Locale("vi", "VN"), "%,.0f", shippingFee)} đ")
                        if (shippingDiscount > 0) {
                            SummaryRow("Giảm giá phí vận chuyển", "-${String.format(Locale("vi", "VN"), "%,.0f", shippingDiscount)} đ", Color.Red)
                        }
                        if (voucherDiscount > 0) {
                            SummaryRow("Voucher từ ReGo", "-${String.format(Locale("vi", "VN"), "%,.0f", voucherDiscount)} đ", Color.Red)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "${String.format(Locale("vi", "VN"), "%,.0f", if (total > 0) total else 0.0)} đ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFF8B4513)
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Giao dịch được bảo mật bởi Marketplace SafePay", color = Color.Gray, fontSize = 11.sp)
                }

                // Small spacer at the bottom
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDiscountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDiscountSheet = false },
            sheetState = discountSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.ConfirmationNumber, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hiện tại chưa có mã giảm giá", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CheckoutCard(
    containerColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun PaymentOption(
    label: String,
    imageUrl: String?,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF00796B) else Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00796B))
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun ShippingOption(
    title: String,
    subtitle: String,
    fee: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    tealColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) tealColor else Color(0xFFEEEEEE))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = if (isSelected) tealColor else Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Text("${String.format(Locale("vi", "VN"), "%,.0f", fee)} đ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
