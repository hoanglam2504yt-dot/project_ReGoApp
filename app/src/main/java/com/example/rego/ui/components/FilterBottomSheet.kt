package com.example.rego.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rego.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialMinPrice: Double?,
    initialMaxPrice: Double?,
    initialSortOption: SortOption,
    onApply: (Double?, Double?, SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    var minPrice by remember { mutableStateOf(initialMinPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(initialMaxPrice?.toString() ?: "") }
    var selectedSort by remember { mutableStateOf(initialSortOption) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Bộ lọc tìm kiếm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Price Range
            Text("Khoảng giá (VNĐ)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Từ") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                Text("-", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Đến") },
                    placeholder = { Text("Ví dụ: 10.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sort Options
            Text("Sắp xếp theo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Column(Modifier.selectableGroup()) {
                SortOptionItem("Mới nhất", selectedSort == SortOption.NEWEST) {
                    selectedSort = SortOption.NEWEST
                }
                SortOptionItem("Giá: Thấp đến Cao", selectedSort == SortOption.PRICE_LOW_HIGH) {
                    selectedSort = SortOption.PRICE_LOW_HIGH
                }
                SortOptionItem("Giá: Cao đến Thấp", selectedSort == SortOption.PRICE_HIGH_LOW) {
                    selectedSort = SortOption.PRICE_HIGH_LOW
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        minPrice = ""
                        maxPrice = ""
                        selectedSort = SortOption.NEWEST
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Thiết lập lại")
                }
                Button(
                    onClick = {
                        onApply(
                            minPrice.toDoubleOrNull(),
                            maxPrice.toDoubleOrNull(),
                            selectedSort
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text("Áp dụng")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SortOptionItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00897B))
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 15.sp
        )
    }
}
