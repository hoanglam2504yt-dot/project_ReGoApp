package com.example.rego.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rego.data.local.entities.Address
import com.example.rego.ui.components.SellDropdownSelection
import com.example.rego.ui.viewmodel.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    viewModel: AddressViewModel,
    userId: Int,
    isSelectionMode: Boolean = false,
    onBackClick: () -> Unit,
    onAddressSelected: (Address) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tealColor = Color(0xFF00695C)
    
    var showAddressForm by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<Address?>(null) }

    LaunchedEffect(userId) {
        viewModel.loadAddresses(userId)
    }

    if (showAddressForm) {
        BackHandler { showAddressForm = false }
        AddressFormFullScreen(
            initialAddress = editingAddress,
            onSave = { name, phone, province, district, ward, houseNumber, isDefault ->
                val newAddress = Address(
                    id = editingAddress?.id ?: 0,
                    userId = userId,
                    recipientName = name,
                    phone = phone,
                    province = province,
                    district = district,
                    ward = ward,
                    houseNumber = houseNumber,
                    isDefault = isDefault
                )
                if (editingAddress == null) {
                    viewModel.addAddress(newAddress)
                } else {
                    viewModel.updateAddress(newAddress)
                }
                showAddressForm = false
            },
            onCancel = { showAddressForm = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Địa chỉ của tôi", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                if (!isSelectionMode) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Button(
                            onClick = { 
                                editingAddress = null
                                showAddressForm = true 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm địa chỉ mới", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF5F5F5))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = tealColor)
                } else if (uiState.addresses.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chưa có địa chỉ nào", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.addresses) { address ->
                            AddressItem(
                                address = address,
                                isSelectionMode = isSelectionMode,
                                onEditClick = {
                                    editingAddress = address
                                    showAddressForm = true
                                },
                                onDeleteClick = { viewModel.deleteAddress(address) },
                                onSetDefault = { viewModel.setDefaultAddress(userId, address.id) },
                                onClick = {
                                    if (isSelectionMode) {
                                        onAddressSelected(address)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormFullScreen(
    initialAddress: Address?,
    onSave: (String, String, String, String, String, String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialAddress?.recipientName ?: "") }
    var phone by remember { mutableStateOf(initialAddress?.phone ?: "") }
    var province by remember { mutableStateOf(initialAddress?.province ?: "TP. Hồ Chí Minh") }
    var district by remember { mutableStateOf(initialAddress?.district ?: "") }
    var ward by remember { mutableStateOf(initialAddress?.ward ?: "") }
    var houseNumber by remember { mutableStateOf(initialAddress?.houseNumber ?: "") }
    var isDefault by remember { mutableStateOf(initialAddress?.isDefault ?: false) }

    val provinces = listOf("TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Hải Phòng")
    
    val districtsMap = mapOf(
        "TP. Hồ Chí Minh" to listOf(
            "Quận 1", "Quận 3", "Quận 4", "Quận 5", "Quận 6", "Quận 7", "Quận 8", "Quận 10", "Quận 11", "Quận 12",
            "Quận Bình Tân", "Quận Bình Thạnh", "Quận Gò Vấp", "Quận Phú Nhuận", "Quận Tân Bình", "Quận Tân Phú",
            "TP. Thủ Đức", "Huyện Bình Chánh", "Huyện Cần Giờ", "Huyện Củ Chi", "Huyện Hóc Môn", "Huyện Nhà Bè"
        ),
        "Hà Nội" to listOf("Quận Ba Đình", "Quận Hoàn Kiếm", "Quận Đống Đa", "Quận Cầu Giấy")
    )

    val wardsMap = mapOf(
        "Quận 1" to listOf("Phường Bến Nghé", "Phường Bến Thành", "Phường Cô Giang", "Phường Cầu Kho", "Phường Cầu Ông Lãnh", "Phường Đa Kao", "Phường Nguyễn Cư Trinh", "Phường Nguyễn Thái Bình", "Phường Tân Định", "Phường Phạm Ngũ Lão"),
        "Quận 3" to listOf("Phường Võ Thị Sáu", "Phường 1", "Phường 2", "Phường 3", "Phường 4", "Phường 5"),
        "TP. Thủ Đức" to listOf("Phường Hiệp Phú", "Phường Hiệp Bình Chánh", "Phường Linh Trung", "Phường Linh Xuân", "Phường Thảo Điền", "Phường An Phú", "Phường Bình Thọ"),
        "Quận Bình Thạnh" to listOf("Phường 1", "Phường 2", "Phường 3", "Phường 11", "Phường 12", "Phường 13", "Phường 28"),
        "Quận Tân Bình" to listOf("Phường 1", "Phường 2", "Phường 12", "Phường 13", "Phường 14", "Phường 15"),
        "Huyện Hóc Môn" to listOf("Xã Bà Điểm", "Xã Xuân Thới Thượng", "Xã Xuân Thới Đông", "Thị trấn Hóc Môn")
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text(if (initialAddress == null) "Thêm địa chỉ mới" else "Chỉnh sửa địa chỉ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = { onSave(name, phone, province, district, ward, houseNumber, isDefault) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)),
                        enabled = name.isNotBlank() && phone.isNotBlank() && district.isNotBlank() && ward.isNotBlank()
                    ) {
                        Text("Lưu địa chỉ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Họ và tên") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            OutlinedTextField(
                value = phone, 
                onValueChange = { phone = it }, 
                label = { Text("Số điện thoại") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            SellDropdownSelection(
                label = "Tỉnh / Thành phố",
                value = province,
                items = provinces,
                onItemSelected = { 
                    province = it
                    district = ""
                    ward = ""
                }
            )

            SellDropdownSelection(
                label = "Quận / Huyện",
                value = if (district.isEmpty()) "Chọn Quận/Huyện" else district,
                items = districtsMap[province] ?: emptyList(),
                onItemSelected = { 
                    district = it
                    ward = ""
                }
            )

            SellDropdownSelection(
                label = "Phường / Xã",
                value = if (ward.isEmpty()) "Chọn Phường/Xã" else ward,
                items = if (district.isEmpty()) emptyList() else (wardsMap[district] ?: listOf("Phường mẫu 1", "Phường mẫu 2")),
                onItemSelected = { ward = it }
            )

            OutlinedTextField(
                value = houseNumber, 
                onValueChange = { houseNumber = it }, 
                label = { Text("Số nhà, tên đường") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text("Ví dụ: 123 Nguyễn Huệ") }
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("Đặt làm địa chỉ mặc định")
            }
        }
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelectionMode: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetDefault: () -> Unit,
    onClick: () -> Unit
) {
    val tealColor = Color(0xFF00695C)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (address.isDefault) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(80.dp)
                        .background(tealColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        address.recipientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("|", color = Color.LightGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(address.phone, color = Color.Gray, fontSize = 14.sp)
                    
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = tealColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Mặc định",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    address.fullAddress,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!address.isDefault && !isSelectionMode) {
                        Text(
                            "Thiết lập mặc định",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { onSetDefault() }
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    if (!isSelectionMode) {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Row(
                        modifier = Modifier.clickable { onEditClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sửa", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
