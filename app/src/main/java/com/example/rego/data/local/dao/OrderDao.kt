package com.example.rego.data.local.dao

import androidx.room.*
import com.example.rego.data.local.entities.Order
import com.example.rego.data.local.entities.OrderItem
import com.example.rego.data.local.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: Order): Long

    @Insert
    suspend fun insertOrderItems(orderItems: List<OrderItem>)

    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY order_date DESC")
    fun getOrdersByUser(userId: Int): Flow<List<Order>>

    @Transaction
    @Query("""
        SELECT p.* FROM products p
        INNER JOIN order_items oi ON p.id = oi.product_id
        WHERE oi.order_id = :orderId
    """)
    fun getProductsForOrder(orderId: Int): Flow<List<Product>>
}
