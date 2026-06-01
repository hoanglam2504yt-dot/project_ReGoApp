package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.entities.Category
import com.example.rego.data.local.entities.Product
import com.example.rego.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    NEWEST, PRICE_LOW_HIGH, PRICE_HIGH_LOW
}

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice = _maxPrice.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.NEWEST)
    val selectedSort = _selectedSort.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sử dụng combine cho mảng Flow để hỗ trợ nhiều hơn 5 luồng
    val products: StateFlow<List<Product>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategoryId,
        _minPrice,
        _maxPrice,
        _selectedSort
    ) { args: Array<Any?> ->
        val allProducts = args[0] as List<Product>
        val query = args[1] as String
        val categoryId = args[2] as Int?
        val min = args[3] as Double?
        val max = args[4] as Double?
        val sort = args[5] as SortOption

        val filtered = allProducts.filter { product ->
            val matchesQuery = product.title.contains(query, ignoreCase = true) ||
                               product.description.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || product.categoryId == categoryId
            val matchesMinPrice = min == null || product.price >= min
            val matchesMaxPrice = max == null || product.price <= max
            matchesQuery && matchesCategory && matchesMinPrice && matchesMaxPrice
        }

        when (sort) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.price }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(categoryId: Int?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun applyFilters(min: Double?, max: Double?, sort: SortOption) {
        _minPrice.value = min
        _maxPrice.value = max
        _selectedSort.value = sort
    }

    fun clearFilters() {
        _minPrice.value = null
        _maxPrice.value = null
        _selectedSort.value = SortOption.NEWEST
        _selectedCategoryId.value = null
    }
}

class HomeViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
