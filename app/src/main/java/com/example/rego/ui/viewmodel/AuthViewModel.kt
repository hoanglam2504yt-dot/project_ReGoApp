package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rego.data.local.UserSession
import com.example.rego.data.local.dao.UserDao
import com.example.rego.data.local.entities.User
import com.example.rego.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
    object PasswordChanged : AuthState()
}

class AuthViewModel(
    private val userDao: UserDao,
    private val userSession: UserSession
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun login(emailOrPhone: String, password: String) {
        if (emailOrPhone.isBlank()) {
            _authState.value = AuthState.Error("Email hoặc số điện thoại không được để trống")
            return
        }
        if (password.isBlank()) {
            _authState.value = AuthState.Error("Mật khẩu không được để trống")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = if (ValidationUtils.isValidEmail(emailOrPhone)) {
                userDao.login(emailOrPhone, password)
            } else {
                userDao.getUserByPhone(emailOrPhone)?.let { 
                    if (it.password == password) it else null
                }
            }

            if (user != null) {
                userSession.saveSession(user.id, user.name, user.email)
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Tài khoản hoặc mật khẩu không chính xác")
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String, address: String) {
        if (name.isBlank()) {
            _authState.value = AuthState.Error("Tên không được để trống")
            return
        }
        if (!ValidationUtils.isValidEmail(email)) {
            _authState.value = AuthState.Error("Email không hợp lệ")
            return
        }
        if (phone.isBlank()) {
            _authState.value = AuthState.Error("Số điện thoại không được để trống")
            return
        }
        if (!ValidationUtils.isValidPassword(password)) {
            _authState.value = AuthState.Error("Mật khẩu phải có ít nhất 6 ký tự")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                _authState.value = AuthState.Error("Email đã được đăng ký")
                return@launch
            }

            val existingPhone = userDao.getUserByPhone(phone)
            if (existingPhone != null) {
                _authState.value = AuthState.Error("Số điện thoại đã được đăng ký")
                return@launch
            }

            val newUser = User(name = name, email = email, password = password, phone = phone, address = address)
            val id = userDao.registerUser(newUser)
            val userWithId = newUser.copy(id = id.toInt())
            userSession.saveSession(userWithId.id, userWithId.name, userWithId.email)
            _authState.value = AuthState.Success(userWithId)
        }
    }

    fun changePassword(userId: Int, currentPass: String, newPass: String, confirmPass: String) {
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }
        if (newPass != confirmPass) {
            _authState.value = AuthState.Error("Mật khẩu xác nhận không khớp")
            return
        }
        if (!ValidationUtils.isValidPassword(newPass)) {
            _authState.value = AuthState.Error("Mật khẩu mới phải có ít nhất 6 ký tự")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = userDao.getUserById(userId).first()
            if (user == null) {
                _authState.value = AuthState.Error("Không tìm thấy người dùng")
                return@launch
            }

            if (user.password != currentPass) {
                _authState.value = AuthState.Error("Mật khẩu hiện tại không chính xác")
                return@launch
            }

            val updatedUser = user.copy(password = newPass)
            userDao.updateUser(updatedUser)
            _authState.value = AuthState.PasswordChanged
        }
    }

    fun logout() {
        viewModelScope.launch {
            userSession.clearSession()
            _authState.value = AuthState.LoggedOut
        }
    }

    fun forgotPassword(email: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            _authState.value = AuthState.Error("Email không hợp lệ")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                _authState.value = AuthState.Error("Yêu cầu đặt lại mật khẩu đã được gửi đến email của bạn") 
            } else {
                _authState.value = AuthState.Error("Email không tồn tại trong hệ thống")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(
    private val userDao: UserDao,
    private val userSession: UserSession
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao, userSession) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
