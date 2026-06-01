package com.example.rego.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.dao.OrderDao
import com.example.rego.data.local.dao.UserDao
import com.example.rego.data.local.entities.Order
import com.example.rego.data.local.entities.Product
import com.example.rego.data.local.entities.User
import com.example.rego.data.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class OrderWithProducts(
    val order: Order,
    val products: List<Product>
)

class ProfileViewModel(
    private val repository: ProductRepository,
    private val userDao: UserDao? = null,
    private val orderDao: OrderDao? = null
) : ViewModel() {

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    fun getUser(userId: Int): Flow<User?> {
        return userDao?.getUserById(userId) ?: flowOf(null)
    }

    fun getProductsBySeller(userId: Int): Flow<List<Product>> = 
        repository.getProductsBySeller(userId)

    fun getFavoriteProducts(userId: Int): Flow<List<Product>> = 
        repository.getFavoriteProducts(userId)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getOrders(userId: Int): Flow<List<OrderWithProducts>> {
        return orderDao?.getOrdersByUser(userId)?.flatMapLatest { orders ->
            if (orders.isEmpty()) return@flatMapLatest flowOf(emptyList())
            
            val orderWithProductsFlows = orders.map { order ->
                orderDao.getProductsForOrder(order.id).map { products ->
                    OrderWithProducts(order, products)
                }
            }
            combine(orderWithProductsFlows) { it.toList() }
        } ?: flowOf(emptyList())
    }

    fun updateProfile(user: User, imageUri: Uri? = null) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            try {
                var finalUser = user
                
                // Cố gắng xử lý Firebase nếu có thể, nếu không thì skip để tránh crash
                try {
                    val storage = FirebaseStorage.getInstance()
                    val firestore = FirebaseFirestore.getInstance()

                    // Upload image if selected
                    imageUri?.let { uri ->
                        user.avatarUrl?.let { oldUrl ->
                            if (oldUrl.contains("firebasestorage")) {
                                try { storage.getReferenceFromUrl(oldUrl).delete().await() } catch (e: Exception) {}
                            }
                        }

                        val ref = storage.reference.child("avatars/${user.id}_${System.currentTimeMillis()}.jpg")
                        ref.putFile(uri).await()
                        val url = ref.downloadUrl.await().toString()
                        finalUser = user.copy(avatarUrl = url)
                    }
                    
                    // Sync to Firestore
                    firestore.collection("users").document(user.id.toString()).set(finalUser)
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Firebase not available, skipping cloud sync: ${e.message}")
                    // Nếu chọn ảnh mới mà không có Firebase, tạm thời dùng local URI (chỉ hiển thị được trên máy này)
                    imageUri?.let { finalUser = finalUser.copy(avatarUrl = it.toString()) }
                }

                // Luôn cập nhật Local Database
                userDao?.updateUser(finalUser)
                
                _updateState.value = ProfileUpdateState.Success
            } catch (e: Exception) {
                _updateState.value = ProfileUpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = ProfileUpdateState.Idle
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch { repository.deleteProduct(productId) }
    }

    fun toggleFavorite(userId: Int, productId: Int, isFavorite: Boolean) {
        viewModelScope.launch { repository.toggleFavorite(userId, productId, isFavorite) }
    }

    fun isFavorite(userId: Int, productId: Int): Flow<Boolean> {
        return repository.isFavorite(userId, productId)
    }
}

sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    object Success : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

class ProfileViewModelFactory(
    private val repository: ProductRepository,
    private val userDao: UserDao? = null,
    private val orderDao: OrderDao? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository, userDao, orderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
