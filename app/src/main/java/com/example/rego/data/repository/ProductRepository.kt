package com.example.rego.data.repository

import com.example.rego.data.local.dao.CategoryDao
import com.example.rego.data.local.dao.ProductDao
import com.example.rego.data.local.dao.FavoriteDao
import com.example.rego.data.local.entities.Category
import com.example.rego.data.local.entities.Product
import com.example.rego.data.local.entities.Favorite
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val favoriteDao: FavoriteDao? = null
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getProductsByCategory(categoryId: Int): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId)
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

    suspend fun insertProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(productId: Int) {
        val product = productDao.getProductById(productId)
        product?.let { productDao.deleteProduct(it) }
    }

    suspend fun updateProductSoldStatus(productId: Int, isSold: Boolean) {
        productDao.updateProductSoldStatus(productId, isSold)
    }

    suspend fun getProductById(productId: Int): Product? {
        return productDao.getProductById(productId)
    }

    fun getProductsBySeller(sellerId: Int): Flow<List<Product>> {
        return productDao.getProductsBySeller(sellerId)
    }

    fun getFavoriteProducts(userId: Int): Flow<List<Product>> {
        return favoriteDao?.getFavoriteProducts(userId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun toggleFavorite(userId: Int, productId: Int, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteDao?.addFavorite(Favorite(userId, productId))
        } else {
            favoriteDao?.removeFavorite(Favorite(userId, productId))
        }
    }

    fun isFavorite(userId: Int, productId: Int): Flow<Boolean> {
        return favoriteDao?.isFavorite(userId, productId) ?: kotlinx.coroutines.flow.flowOf(false)
    }
}
