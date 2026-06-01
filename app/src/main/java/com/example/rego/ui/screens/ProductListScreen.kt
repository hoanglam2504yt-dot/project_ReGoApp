package com.example.rego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rego.ui.components.MarketProductItem
import com.example.rego.ui.components.FilterBottomSheet
import com.example.rego.ui.viewmodel.HomeViewModel
import com.example.rego.ui.viewmodel.CartViewModel
import com.example.rego.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    profileViewModel: ProfileViewModel,
    isLoggedIn: Boolean,
    userId: Int,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val tealColor = Color(0xFF00796B)

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
                title = { Text("Tất cả sản phẩm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .background(Color(0xFFF5F5F5))
        ) {
            // Search and Filter Header
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm kiếm sản phẩm...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = tealColor
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Categories Row
                Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text("Tất cả") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tealColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelected(category.id) },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tealColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${products.size} sản phẩm tìm thấy", color = Color.Gray, fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.clickable { showFilterSheet = true }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                            Text(" Bộ lọc", color = tealColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(products) { product ->
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
        }
    }
}
