package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.dao.*
import com.example.rego.data.local.entities.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val products: List<Pair<Product, Int>> = emptyList(),
    val seller: User? = null,
    val user: User? = null,
    val shippingAddress: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val shippingMethod: String = "Giao hàng hỏa tốc",
    val shippingFee: Double = 35000.0,
    val discount: Double = 15000.0,
    val paymentMethod: String = "Thẻ ngân hàng (Visa/Master)",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class CheckoutViewModel(
    private val userDao: UserDao,
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val addressDao: AddressDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState = _uiState.asStateFlow()

    fun loadCheckoutData(userId: Int, productId: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val user = userDao.getUserById(userId).first()
            val defaultAddress = addressDao.getDefaultAddress(userId).first()
            
            val productsWithQuantity = if (productId != null) {
                val product = productDao.getProductById(productId)
                if (product != null) listOf(product to 1) else emptyList()
            } else {
                val cartItems = cartDao.getCartItems(userId).first()
                val products = cartDao.getCartProducts(userId).first()
                products.map { product ->
                    val quantity = cartItems.find { it.productId == product.id }?.quantity ?: 1
                    product to quantity
                }
            }

            val seller = if (productsWithQuantity.isNotEmpty()) {
                userDao.getUserById(productsWithQuantity.first().first.sellerId).first()
            } else null

            _uiState.update { 
                it.copy(
                    user = user,
                    products = productsWithQuantity,
                    seller = seller,
                    shippingAddress = defaultAddress?.fullAddress ?: user?.address ?: "",
                    recipientName = defaultAddress?.recipientName ?: user?.name ?: "",
                    recipientPhone = defaultAddress?.phone ?: user?.phone ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun selectAddress(address: Address) {
        _uiState.update {
            it.copy(
                recipientName = address.recipientName,
                recipientPhone = address.phone,
                shippingAddress = address.fullAddress
            )
        }
    }

    fun placeOrder(userId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val subtotal = currentState.products.sumOf { it.first.price * it.second }
            val totalAmount = subtotal + currentState.shippingFee - currentState.discount
            
            val order = Order(
                userId = userId,
                totalAmount = totalAmount,
                shippingAddress = currentState.shippingAddress,
                status = "Đang xử lý"
            )
            
            val orderId = orderDao.insertOrder(order).toInt()
            
            val orderItems = currentState.products.map { (product, quantity) ->
                OrderItem(
                    orderId = orderId,
                    productId = product.id,
                    quantity = quantity,
                    price = product.price
                )
            }
            
            orderDao.insertOrderItems(orderItems)
            
            _uiState.update { it.copy(isSuccess = true) }
            onComplete()
        }
    }

    fun setShippingMethod(method: String, fee: Double) {
        _uiState.update { 
            it.copy(
                shippingMethod = method,
                shippingFee = fee
            )
        }
    }

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }
}

class CheckoutViewModelFactory(
    private val userDao: UserDao,
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val addressDao: AddressDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(userDao, productDao, cartDao, orderDao, addressDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
