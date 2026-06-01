package com.example.rego.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    open val routePattern: String get() = route

    object Login : Screen("login", "Đăng nhập")
    object Register : Screen("register", "Đăng ký")
    object Home : Screen("home", "Trang chính", Icons.Default.Home)
    object Products : Screen("products", "Sản phẩm", Icons.Default.Storefront)
    
    object Sell : Screen("sell", "Bán", Icons.Default.AddCircle) {
        override val routePattern: String = "sell?productId={productId}"
        fun createRoute(productId: Int? = null) = if (productId != null) "sell?productId=$productId" else "sell"
    }
    
    object Chat : Screen("chat", "Trò chuyện", Icons.Default.Chat)
    object Profile : Screen("profile", "Hồ sơ", Icons.Default.Person)
    object Settings : Screen("settings", "Cài đặt", Icons.Default.Settings)
    object EditProfile : Screen("edit_profile", "Chỉnh sửa hồ sơ")
    object ChangePassword : Screen("change_password", "Đổi mật khẩu")
    
    object MyAddresses : Screen("my_addresses", "Địa chỉ của tôi") {
        override val routePattern: String = "my_addresses?isSelectionMode={isSelectionMode}"
        fun createRoute(isSelectionMode: Boolean = false) = "my_addresses?isSelectionMode=$isSelectionMode"
    }
    
    object Categories : Screen("categories", "Danh mục")
    object ProductDetail : Screen("product_detail", "Chi tiết sản phẩm") {
        override val routePattern: String = "product_detail/{productId}"
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    
    object OtherProfile : Screen("other_profile", "Trang cá nhân") {
        override val routePattern: String = "other_profile/{userId}"
        fun createRoute(userId: Int) = "other_profile/$userId"
    }
    
    object Cart : Screen("cart", "Giỏ hàng")
    
    object Checkout : Screen("checkout", "Thanh toán") {
        override val routePattern: String = "checkout?productId={productId}"
        fun createRoute(productId: String? = null) = if (productId != null) "checkout?productId=$productId" else "checkout"
    }
    
    object Notifications : Screen("notifications", "Thông báo")
    
    object MessageDetail : Screen("message_detail", "Chi tiết tin nhắn") {
        override val routePattern: String = "message_detail/{chatId}"
        fun createRoute(chatId: String) = "message_detail/$chatId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Products,
    Screen.Sell,
    Screen.Chat,
    Screen.Settings
)
