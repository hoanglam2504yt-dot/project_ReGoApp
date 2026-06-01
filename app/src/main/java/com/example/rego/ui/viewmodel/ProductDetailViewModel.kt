package com.example.rego.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.dao.CartDao
import com.example.rego.data.local.dao.ProductDao
import com.example.rego.data.local.dao.UserDao
import com.example.rego.data.local.entities.Cart
import com.example.rego.data.local.entities.Product
import com.example.rego.data.local.entities.User
import com.example.rego.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val repository: ProductRepository
) : ViewModel() {

    // Wrap Firebase instances in lazy and try-catch to prevent crash if not initialized
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _product = MutableStateFlow<Product?>(null)
    val product = _product.asStateFlow()

    private val _seller = MutableStateFlow<User?>(null)
    val seller = _seller.asStateFlow()

    // LiveData for Fragment usage as requested
    private val _sellerFirestore = MutableLiveData<User?>()
    val sellerFirestore: LiveData<User?> = _sellerFirestore

    private val _chatRoomId = MutableLiveData<String>()
    val chatRoomId: LiveData<String> = _chatRoomId

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            val p = productDao.getProductById(productId)
            _product.value = p
            p?.let {
                _seller.value = userDao.getUserById(it.sellerId).first()
                // Fetch seller from Firestore if needed
                fetchSellerFromFirestore(it.sellerId.toString())
            }
        }
    }

    fun markAsSold(productId: Int) {
        viewModelScope.launch {
            repository.updateProductSoldStatus(productId, true)
            // Reload product to update UI
            loadProduct(productId)
        }
    }

    private fun fetchSellerFromFirestore(ownerId: String) {
        db?.collection("users")?.document(ownerId)?.get()
            ?.addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                _sellerFirestore.value = user
            }
    }

    fun getOrCreateChatRoom(sellerId: String) {
        val currentUserId = auth?.currentUser?.uid ?: "temp_user_id" // Fallback for demo
        val participants = listOf(currentUserId, sellerId).sorted()

        db?.collection("chatRooms")
            ?.whereEqualTo("participants", participants)
            ?.get()
            ?.addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    _chatRoomId.value = documents.documents[0].id
                } else {
                    val newRoomRef = db?.collection("chatRooms")?.document()
                    if (newRoomRef != null) {
                        val data = mapOf("id" to newRoomRef.id, "participants" to participants)
                        newRoomRef.set(data).addOnSuccessListener {
                            _chatRoomId.value = newRoomRef.id
                        }
                    }
                }
            }
    }

    fun addToCart(userId: Int, productId: Int) {
        viewModelScope.launch {
            cartDao.addToCart(Cart(userId = userId, productId = productId, quantity = 1))
        }
    }

    fun isFavorite(userId: Int, productId: Int) = repository.isFavorite(userId, productId)

    fun toggleFavorite(userId: Int, productId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(userId, productId, isFavorite)
        }
    }
}

class ProductDetailViewModelFactory(
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductDetailViewModel(productDao, cartDao, userDao, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
