package com.example.rego.data.local.dao

import androidx.room.*
import com.example.rego.data.local.entities.Favorite
import com.example.rego.data.local.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Delete
    suspend fun removeFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorites WHERE user_id = :userId")
    fun getFavoritesByUserId(userId: Int): Flow<List<Favorite>>

    @Query("""
        SELECT products.* FROM products 
        INNER JOIN favorites ON products.id = favorites.product_id 
        WHERE favorites.user_id = :userId
    """)
    fun getFavoriteProducts(userId: Int): Flow<List<Product>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = :userId AND product_id = :productId)")
    fun isFavorite(userId: Int, productId: Int): Flow<Boolean>
}
