package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.dao.CartDao
import com.example.rego.data.local.dao.UserDao
import com.example.rego.data.local.entities.Cart
import com.example.rego.data.local.entities.Product
import com.example.rego.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    val quantity: Int,
    val sellerName: String,
    val sellerAvatar: String,
    val isSelected: Boolean,
    val isFavorite: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModel(
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val repository: ProductRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<Int>(1) 
    fun setUserId(userId: Int) {
        if (userId != 0) _userId.value = userId
    }

    private val _selectedItemIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItemIds = _selectedItemIds.asStateFlow()

    private var isInitialSelectionDone = false

    val cartItems: StateFlow<List<CartItem>> = _userId
        .flatMapLatest { userId ->
            isInitialSelectionDone = false 
            cartDao.getCartItems(userId)
                .combine(cartDao.getCartProducts(userId)) { items, products ->
                    items to products
                }
                .flatMapLatest { (items, products) ->
                    if (products.isEmpty()) return@flatMapLatest flowOf(emptyList<CartItem>())
                    
                    combine(
                        products.map { product ->
                            val cart = items.find { it.productId == product.id }
                            val quantity = cart?.quantity ?: 0
                            
                            combine(
                                userDao.getUserById(product.sellerId),
                                repository.isFavorite(userId, product.id),
                                _selectedItemIds
                            ) { seller, isFav, selectedIds ->
                                CartItem(
                                    product = product,
                                    quantity = quantity,
                                    sellerName = seller?.name ?: "Người bán ReGo",
                                    sellerAvatar = "https://i.pravatar.cc/150?u=${product.sellerId}", 
                                    isSelected = selectedIds.contains(product.id),
                                    isFavorite = isFav
                                )
                            }
                        }
                    ) { it.toList() }
                }
        }
        .onEach { items ->
            if (!isInitialSelectionDone && items.isNotEmpty()) {
                _selectedItemIds.value = items.map { it.product.id }.toSet()
                isInitialSelectionDone = true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPrice: StateFlow<Double> = cartItems.map { items ->
        items.filter { it.isSelected }.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun toggleSelection(productId: Int) {
        val current = _selectedItemIds.value
        if (current.contains(productId)) {
            _selectedItemIds.value = current - productId
        } else {
            _selectedItemIds.value = current + productId
        }
    }

    fun toggleSelectAll() {
        val items = cartItems.value
        if (items.isEmpty()) return
        
        val allIdsInCart = items.map { it.product.id }.toSet()
        val areAllInCartSelected = items.all { it.isSelected }
        
        if (areAllInCartSelected) {
            _selectedItemIds.value = _selectedItemIds.value - allIdsInCart
        } else {
            _selectedItemIds.value = _selectedItemIds.value + allIdsInCart
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            val userId = _userId.value
            val currentItems = cartItems.value
            val existingItem = currentItems.find { it.product.id == productId }
            
            if (existingItem != null) {
                cartDao.updateCartQuantity(Cart(userId, productId, existingItem.quantity + 1))
            } else {
                cartDao.addToCart(Cart(userId, productId, 1))
            }
        }
    }

    fun updateQuantity(productId: Int, newQuantity: Int) {
        viewModelScope.launch {
            val userId = _userId.value
            if (newQuantity <= 0) {
                removeItem(productId)
            } else {
                cartDao.updateCartQuantity(Cart(userId, productId, newQuantity))
            }
        }
    }

    fun removeItem(productId: Int) {
        viewModelScope.launch {
            cartDao.removeFromCart(Cart(_userId.value, productId, 0))
            _selectedItemIds.value = _selectedItemIds.value - productId
        }
    }

    fun toggleFavorite(productId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(_userId.value, productId, isFavorite)
        }
    }
}

class CartViewModelFactory(
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(cartDao, userDao, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
