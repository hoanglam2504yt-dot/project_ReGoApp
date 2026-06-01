package com.example.rego.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rego.data.local.entities.User
import com.example.rego.ui.viewmodel.ProfileUpdateState
import com.example.rego.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    userId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val tealColor = Color(0xFF00897B)
    val user by viewModel.getUser(userId).collectAsState(initial = null)
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(user) {
        user?.let {
            name = it.name
            phone = it.phone
            email = it.email
            birthday = it.birthday ?: ""
            gender = it.gender ?: "Nam"
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is ProfileUpdateState.Success -> {
                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                onBackClick()
            }
            is ProfileUpdateState.Error -> {
                Toast.makeText(context, "Lỗi: ${(updateState as ProfileUpdateState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold, color = tealColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tealColor)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { imagePickerLauncher.launch("image/*") }
            ) {
                AsyncImage(
                    model = selectedImageUri ?: user?.avatarUrl ?: "https://via.placeholder.com/150",
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .background(tealColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Text(
                "Thay đổi ảnh đại diện",
                color = tealColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Form Fields
            EditField(label = "Họ và tên", value = name, onValueChange = { name = it })
            
            EditField(
                label = "Số điện thoại", 
                value = phone, 
                onValueChange = { phone = it },
                trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = tealColor) }
            )
            
            EditField(
                label = "Email", 
                value = email, 
                onValueChange = {}, 
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.LightGray) }
            )
            
            EditField(
                label = "Ngày sinh", 
                value = birthday, 
                onValueChange = { birthday = it },
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendar") }
            )

            // Gender Selection
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Giới tính", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Nam", "Nữ", "Khác").forEach { option ->
                        val isSelected = gender == option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp)
                                .clickable { gender = option },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) tealColor.copy(alpha = 0.1f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) tealColor else Color.LightGray)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    option,
                                    color = if (isSelected) tealColor else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (updateState is ProfileUpdateState.Loading) {
                CircularProgressIndicator(color = tealColor)
            } else {
                Button(
                    onClick = {
                        user?.let {
                            val updatedUser = it.copy(
                                name = name,
                                phone = phone,
                                birthday = birthday,
                                gender = gender
                            )
                            viewModel.updateProfile(updatedUser, selectedImageUri)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00897B),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = if (readOnly) Color(0xFFF5F5F5) else Color.Transparent,
                unfocusedContainerColor = if (readOnly) Color(0xFFF5F5F5) else Color.Transparent
            ),
            trailingIcon = trailingIcon,
            singleLine = true
        )
    }
}
