package com.example.rego.data.local.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["seller_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    @ColumnInfo(name = "is_gift") val isGift: Boolean = false,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "seller_id") val sellerId: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "image_url_2") val imageUrl2: String? = null,
    @ColumnInfo(name = "image_url_3") val imageUrl3: String? = null,
    val location: String = "Hà Nội",
    
    // Các trường thông tin mới theo yêu cầu
    val condition: String = "Mới",
    val brand: String = "",
    val model: String = "",
    @ColumnInfo(name = "spec_1_name") val spec1Name: String = "",
    @ColumnInfo(name = "spec_1_value") val spec1Value: String = "",
    @ColumnInfo(name = "spec_2_name") val spec2Name: String = "",
    @ColumnInfo(name = "spec_2_value") val spec2Value: String = "",
    @ColumnInfo(name = "is_verified") val isVerified: Boolean = false,
    @ColumnInfo(name = "shipping_info") val shippingInfo: String = "Giao hàng trong 24h",
    @ColumnInfo(name = "is_sold") val isSold: Boolean = false,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) : Parcelable
