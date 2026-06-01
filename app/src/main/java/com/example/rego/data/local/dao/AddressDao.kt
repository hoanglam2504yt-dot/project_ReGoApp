package com.example.rego.data.local.dao

import androidx.room.*
import com.example.rego.data.local.entities.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId")
    fun getAddressesByUserId(userId: Int): Flow<List<Address>>

    @Query("SELECT * FROM addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    fun getDefaultAddress(userId: Int): Flow<Address?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: Address)

    @Update
    suspend fun updateAddress(address: Address)

    @Delete
    suspend fun deleteAddress(address: Address)

    @Transaction
    suspend fun setDefaultAddress(userId: Int, addressId: Int) {
        // Reset all addresses for this user to not default
        resetDefaultAddresses(userId)
        // Set the specific address to default
        updateIsDefault(addressId, true)
    }

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun resetDefaultAddresses(userId: Int)

    @Query("UPDATE addresses SET isDefault = :isDefault WHERE id = :addressId")
    suspend fun updateIsDefault(addressId: Int, isDefault: Boolean)
}
