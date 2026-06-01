package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.entities.Category
import com.example.rego.data.local.entities.Product
import com.example.rego.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SellState {
    object Idle : SellState()
    object Loading : SellState()
    object Success : SellState()
    data class Error(val message: String) : SellState()
}

@HiltViewModel
class SellViewModel @Inject constructor(private val repository: ProductRepository) : ViewModel() {
    private val _sellState = MutableStateFlow<SellState>(SellState.Idle)
    val sellState = _sellState.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct = _editingProduct.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadProductForEditing(productId: Int) {
        viewModelScope.launch {
            val product = repository.getProductById(productId)
            _editingProduct.value = product
        }
    }

    fun postProduct(
        id: Int = 0,
        title: String,
        description: String,
        price: Double,
        isGift: Boolean,
        categoryId: Int,
        sellerId: Int,
        imageUrl: String,
        imageUrl2: String? = null,
        imageUrl3: String? = null,
        location: String,
        condition: String = "Mới",
        brand: String = "",
        model: String = "",
        spec1Name: String = "",
        spec1Value: String = "",
        spec2Name: String = "",
        spec2Value: String = "",
        isVerified: Boolean = false,
        shippingInfo: String = "Giao hàng trong 24h"
    ) {
        if (title.isBlank() || description.isBlank() || categoryId == 0 || location.isBlank()) {
            _sellState.value = SellState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        viewModelScope.launch {
            _sellState.value = SellState.Loading
            try {
                // Get current sold status if editing
                val currentIsSold = if (id != 0) {
                    repository.getProductById(id)?.isSold ?: false
                } else {
                    false
                }

                val product = Product(
                    id = id,
                    title = title,
                    description = description,
                    price = if (isGift) 0.0 else price,
                    isGift = isGift,
                    categoryId = categoryId,
                    sellerId = sellerId,
                    imageUrl = imageUrl,
                    imageUrl2 = imageUrl2,
                    imageUrl3 = imageUrl3,
                    location = location,
                    condition = condition,
                    brand = brand,
                    model = model,
                    spec1Name = spec1Name,
                    spec1Value = spec1Value,
                    spec2Name = spec2Name,
                    spec2Value = spec2Value,
                    isVerified = isVerified,
                    shippingInfo = shippingInfo,
                    isSold = currentIsSold
                )
                if (id == 0) {
                    repository.insertProduct(product)
                } else {
                    repository.updateProduct(product)
                }
                _sellState.value = SellState.Success
            } catch (e: Exception) {
                _sellState.value = SellState.Error("Lỗi: ${e.message}")
            }
        }
    }

    fun resetState() {
        _sellState.value = SellState.Idle
        _editingProduct.value = null
    }
}

class SellViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SellViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
