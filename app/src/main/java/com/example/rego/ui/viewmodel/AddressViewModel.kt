package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.dao.AddressDao
import com.example.rego.data.local.entities.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddressUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AddressViewModel(private val addressDao: AddressDao) : ViewModel() {
    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    fun loadAddresses(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            addressDao.getAddressesByUserId(userId).collect { list ->
                _uiState.value = _uiState.value.copy(addresses = list, isLoading = false)
            }
        }
    }

    fun addAddress(address: Address) {
        viewModelScope.launch {
            if (address.isDefault) {
                addressDao.resetDefaultAddresses(address.userId)
            }
            addressDao.insertAddress(address)
        }
    }

    fun updateAddress(address: Address) {
        viewModelScope.launch {
            if (address.isDefault) {
                addressDao.resetDefaultAddresses(address.userId)
            }
            addressDao.updateAddress(address)
        }
    }

    fun deleteAddress(address: Address) {
        viewModelScope.launch {
            addressDao.deleteAddress(address)
        }
    }

    fun setDefaultAddress(userId: Int, addressId: Int) {
        viewModelScope.launch {
            addressDao.setDefaultAddress(userId, addressId)
        }
    }
}

class AddressViewModelFactory(private val addressDao: AddressDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddressViewModel(addressDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
