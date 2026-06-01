package com.example.rego.data.local.dao

import androidx.room.*
import com.example.rego.data.local.entities.Cart
import com.example.rego.data.local.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cart: Cart)

    @Update
    suspend fun updateCartQuantity(cart: Cart)

    @Delete
    suspend fun removeFromCart(cart: Cart)

    @Query("SELECT * FROM cart WHERE user_id = :userId")
    fun getCartItems(userId: Int): Flow<List<Cart>>

    @Query("""
        SELECT products.* FROM products 
        INNER JOIN cart ON products.id = cart.product_id 
        WHERE cart.user_id = :userId
    """)
    fun getCartProducts(userId: Int): Flow<List<Product>>

    @Query("DELETE FROM cart WHERE user_id = :userId")
    suspend fun clearCart(userId: Int)
}
