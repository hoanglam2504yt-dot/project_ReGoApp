package com.example.rego.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.rego.ui.components.SellDropdownSelection
import com.example.rego.ui.viewmodel.SellState
import com.example.rego.ui.viewmodel.SellViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    viewModel: SellViewModel,
    userId: Int,
    productId: Int? = null,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val editingProduct by viewModel.editingProduct.collectAsState()

    var productName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var selectedCategoryName by remember { mutableStateOf("Chọn danh mục") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isGift by remember { mutableStateOf(false) }
    
    var condition by remember { mutableStateOf("Mới") }
    var brand by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var spec1Name by remember { mutableStateOf("") }
    var spec1Value by remember { mutableStateOf("") }
    var spec2Name by remember { mutableStateOf("") }
    var spec2Value by remember { mutableStateOf("") }
    var shippingInfo by remember { mutableStateOf("Giao hàng trong 24h") }

    // Image Handling
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImageUrls by remember { mutableStateOf<List<String?>>(listOf(null, null, null)) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    val sellState by viewModel.sellState.collectAsState()

    LaunchedEffect(productId) {
        if (productId != null && productId != 0) {
            viewModel.loadProductForEditing(productId)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(editingProduct) {
        editingProduct?.let { p ->
            productName = p.title
            selectedCategoryId = p.categoryId
            selectedCategoryName = categories.find { it.id == p.categoryId }?.name ?: "Chọn danh mục"
            price = p.price.toInt().toString()
            description = p.description
            isGift = p.isGift
            condition = p.condition
            brand = p.brand
            modelName = p.model
            spec1Name = p.spec1Name
            spec1Value = p.spec1Value
            spec2Name = p.spec2Name
            spec2Value = p.spec2Value
            shippingInfo = p.shippingInfo
            existingImageUrls = listOf(p.imageUrl, p.imageUrl2, p.imageUrl3)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { if (selectedImageUris.size < 3) selectedImageUris = selectedImageUris + it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempImageUri?.let { if (selectedImageUris.size < 3) selectedImageUris = selectedImageUris + it }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Cần quyền máy ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    // Address states
    val provinces = listOf("TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng")
    val districtsMap = mapOf(
        "TP. Hồ Chí Minh" to listOf("Quận 1", "Quận 3", "Quận 10", "Quận Tân Bình", "TP. Thủ Đức"),
        "Hà Nội" to listOf("Quận Hoàn Kiếm", "Quận Đống Đa", "Quận Cầu Giấy"),
        "Đà Nẵng" to listOf("Quận Hải Châu", "Quận Thanh Khê")
    )
    val wardsMap = mapOf(
        "Quận 1" to listOf("Phường Bến Nghé", "Phường Bến Thành", "Phường Đa Kao"),
        "Quận 3" to listOf("Phường Võ Thị Sáu", "Phường 5"),
        "Quận 10" to listOf("Phường 1", "Phường 12"),
        "Quận Tân Bình" to listOf("Phường 2", "Phường 13"),
        "TP. Thủ Đức" to listOf("Phường Linh Trung", "Phường Thảo Điền")
    )

    var selectedProvince by remember { mutableStateOf(provinces[0]) }
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedWard by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }

    LaunchedEffect(sellState) {
        if (sellState is SellState.Success) {
            Toast.makeText(context, "Đã lưu tin đăng!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBackClick()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Thêm hình ảnh", fontWeight = FontWeight.Bold) },
            text = { Text("Chọn ảnh từ thư viện hoặc chụp ảnh mới (${selectedImageUris.size}/3).") },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA); showImageSourceDialog = false }) {
                    Text("Máy ảnh")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*"); showImageSourceDialog = false }) {
                    Text("Thư viện")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId != null) "Sửa tin đăng" else "Đăng tin", color = Color(0xFF00796B), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = {
                        val fullAddress = if (houseNumber.isNotBlank() && selectedWard.isNotBlank()) {
                            "$houseNumber, $selectedWard, $selectedDistrict, $selectedProvince"
                        } else {
                            editingProduct?.location ?: "Chưa cập nhật địa chỉ"
                        }
                        
                        val img1 = if (selectedImageUris.isNotEmpty()) selectedImageUris[0].toString() else (existingImageUrls[0] ?: "https://images.unsplash.com/photo-1523275335684-37898b6baf30")
                        val img2 = if (selectedImageUris.size > 1) selectedImageUris[1].toString() else existingImageUrls[1]
                        val img3 = if (selectedImageUris.size > 2) selectedImageUris[2].toString() else existingImageUrls[2]

                        viewModel.postProduct(
                            id = productId ?: 0, title = productName, description = description, price = if (isGift) 0.0 else price.toDoubleOrNull() ?: 0.0,
                            isGift = isGift, categoryId = selectedCategoryId, sellerId = userId, imageUrl = img1, imageUrl2 = img2, imageUrl3 = img3,
                            location = fullAddress, condition = condition, brand = brand, model = modelName, spec1Name = spec1Name, spec1Value = spec1Value,
                            spec2Name = spec2Name, spec2Value = spec2Value, isVerified = true, shippingInfo = shippingInfo
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                    enabled = sellState !is SellState.Loading && productName.isNotBlank() && selectedCategoryId != 0
                ) {
                    if (sellState is SellState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Lưu tin đăng", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Column {
                    Text("Hình ảnh (${selectedImageUris.size}/3)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { index ->
                            val uri = selectedImageUris.getOrNull(index)
                            ImagePlaceholder(
                                uri = uri,
                                existingUrl = if (uri == null) existingImageUrls.getOrNull(index) else null,
                                isMain = index == 0,
                                modifier = Modifier.weight(1f),
                                onClick = { if (selectedImageUris.size < 3) showImageSourceDialog = true }
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SellInputField(label = "Tên sản phẩm", value = productName, onValueChange = { productName = it }, placeholder = "VD: Điện thoại iPhone 13 Pro Max")
                    
                    var showCategoryMenu by remember { mutableStateOf(false) }
                    Column {
                        Text("Danh mục", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                        Box {
                            OutlinedTextField(
                                value = selectedCategoryName, onValueChange = {}, readOnly = true, enabled = false,
                                modifier = Modifier.fillMaxWidth().clickable { showCategoryMenu = true },
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray)
                            )
                            DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; selectedCategoryName = cat.name; showCategoryMenu = false })
                                }
                            }
                        }
                    }

                    SellDropdownSelection(label = "Tình trạng", value = condition, items = listOf("Mới", "Như mới", "Đã sử dụng (tốt)", "Cũ"), onItemSelected = { condition = it })
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SellInputField(label = "Thương hiệu", value = brand, onValueChange = { brand = it }, placeholder = "VD: Apple", modifier = Modifier.weight(1f))
                        SellInputField(label = "Dòng máy/Model", value = modelName, onValueChange = { modelName = it }, placeholder = "VD: iPhone 13", modifier = Modifier.weight(1f))
                    }

                    Column {
                        Text("Thông số kỹ thuật (Tùy chọn)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SellInputField(label = "Tên thông số 1", value = spec1Name, onValueChange = { spec1Name = it }, placeholder = "VD: RAM", modifier = Modifier.weight(1f))
                            SellInputField(label = "Giá trị 1", value = spec1Value, onValueChange = { spec1Value = it }, placeholder = "VD: 8GB", modifier = Modifier.weight(1f))
                        }
                    }

                    Column {
                        Text("Giá", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = if (isGift) "0" else price, onValueChange = { if (!isGift) price = it },
                            modifier = Modifier.fillMaxWidth(), placeholder = { Text("Nhập giá sản phẩm") },
                            trailingIcon = { Text("đ", fontWeight = FontWeight.Bold) },
                            enabled = !isGift, shape = RoundedCornerShape(8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp).clickable { isGift = !isGift }) {
                            Checkbox(checked = isGift, onCheckedChange = { isGift = it })
                            Text("Tôi muốn tặng miễn phí sản phẩm này")
                        }
                    }

                    SellDropdownSelection(label = "Thông tin vận chuyển", value = shippingInfo, items = listOf("Giao hàng trong 24h", "2-3 ngày", "Người bán tự giao"), onItemSelected = { shippingInfo = it })

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Địa chỉ", fontWeight = FontWeight.Bold)
                        SellDropdownSelection(label = "Tỉnh / Thành phố", value = selectedProvince, items = provinces, onItemSelected = { selectedProvince = it; selectedDistrict = ""; selectedWard = "" })
                        SellDropdownSelection(label = "Quận / Huyện", value = if(selectedDistrict.isEmpty()) "Chọn Quận" else selectedDistrict, items = districtsMap[selectedProvince] ?: emptyList(), onItemSelected = { selectedDistrict = it; selectedWard = "" })
                        SellDropdownSelection(label = "Phường / Xã", value = if(selectedWard.isEmpty()) "Chọn Phường" else selectedWard, items = if(selectedDistrict.isEmpty()) emptyList() else (wardsMap[selectedDistrict] ?: emptyList()), onItemSelected = { selectedWard = it })
                        SellInputField(label = "Số nhà, tên đường", value = houseNumber, onValueChange = { houseNumber = it }, placeholder = "VD: 123 Nguyễn Huệ")
                    }

                    SellInputField(label = "Mô tả chi tiết", value = description, onValueChange = { description = it }, placeholder = "Mô tả tình trạng, phụ kiện kèm theo...", singleLine = false, minLines = 4)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

fun createImageUri(context: android.content.Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES))
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun ImagePlaceholder(uri: Uri?, existingUrl: String?, isMain: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)).border(1.dp, if(isMain) Color(0xFF00796B) else Color.Transparent, RoundedCornerShape(8.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        if (uri != null) AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else if (!existingUrl.isNullOrBlank()) AsyncImage(model = existingUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(if (isMain) Icons.Default.AddAPhoto else Icons.Default.Image, null, tint = if(isMain) Color(0xFF00796B) else Color.Gray)
            if (isMain) Text("Ảnh chính", fontSize = 10.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SellInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, singleLine: Boolean = true, minLines: Int = 1, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            shape = RoundedCornerShape(8.dp), singleLine = singleLine, minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00796B), unfocusedBorderColor = Color.LightGray)
        )
    }
}
