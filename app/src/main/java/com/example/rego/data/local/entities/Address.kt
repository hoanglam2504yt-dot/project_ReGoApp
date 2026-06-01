package com.example.rego.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "addresses")
data class Address(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val recipientName: String,
    val phone: String,
    val province: String,
    val district: String,
    val ward: String,
    val houseNumber: String,
    val isDefault: Boolean = false
) : Parcelable {
    val fullAddress: String
        get() = "${if (houseNumber.isNotBlank()) "$houseNumber, " else ""}$ward, $district, $province"
}
