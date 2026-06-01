package com.example.rego.data.local

import com.example.rego.data.local.dao.CategoryDao
import com.example.rego.data.local.dao.ProductDao
import com.example.rego.data.local.dao.UserDao
import com.example.rego.data.local.entities.Category
import com.example.rego.data.local.entities.User
import kotlinx.coroutines.flow.first

object DatabaseSeeder {
    suspend fun seed(userDao: UserDao, categoryDao: CategoryDao, productDao: ProductDao) {
        // 1. Tạo danh sách 10 User mẫu để dùng vĩnh viễn
        val users: List<User> = userDao.getAllUsers().first()
        if (users.size <= 1) { // Nếu chỉ có admin hoặc trống
            val sampleUsers = listOf(
                User(id = 1, name = "Admin ReGo", email = "admin@rego.com", password = "password123", phone = "0123456789", address = "TP. Hồ Chí Minh", avatarUrl = "https://randomuser.me/api/portraits/men/1.jpg"),
                User(id = 2, name = "Nguyễn Văn A", email = "user1@rego.com", password = "password123", phone = "0901234561", address = "Hà Nội", avatarUrl = "https://randomuser.me/api/portraits/men/2.jpg"),
                User(id = 3, name = "Trần Thị B", email = "user2@rego.com", password = "password123", phone = "0901234562", address = "Đà Nẵng", avatarUrl = "https://randomuser.me/api/portraits/women/3.jpg"),
                User(id = 4, name = "Lê Văn C", email = "user3@rego.com", password = "password123", phone = "0901234563", address = "Cần Thơ", avatarUrl = "https://randomuser.me/api/portraits/men/4.jpg"),
                User(id = 5, name = "Phạm Thị D", email = "user4@rego.com", password = "password123", phone = "0901234564", address = "Hải Phòng", avatarUrl = "https://randomuser.me/api/portraits/women/5.jpg"),
                User(id = 6, name = "Hoàng Văn E", email = "user5@rego.com", password = "password123", phone = "0901234565", address = "Huế", avatarUrl = "https://randomuser.me/api/portraits/men/6.jpg"),
                User(id = 7, name = "Vũ Thị F", email = "user6@rego.com", password = "password123", phone = "0901234566", address = "Nha Trang", avatarUrl = "https://randomuser.me/api/portraits/women/7.jpg"),
                User(id = 8, name = "Đặng Văn G", email = "user7@rego.com", password = "password123", phone = "0901234567", address = "Vũng Tàu", avatarUrl = "https://randomuser.me/api/portraits/men/8.jpg"),
                User(id = 9, name = "Bùi Thị H", email = "user8@rego.com", password = "password123", phone = "0901234568", address = "Đà Lạt", avatarUrl = "https://randomuser.me/api/portraits/women/9.jpg"),
                User(id = 10, name = "Lý Văn I", email = "user9@rego.com", password = "password123", phone = "0901234569", address = "Biên Hòa", avatarUrl = "https://randomuser.me/api/portraits/men/10.jpg"),
                User(id = 11, name = "Mai Thị K", email = "user10@rego.com", password = "password123", phone = "0901234570", address = "Buôn Ma Thuột", avatarUrl = "https://randomuser.me/api/portraits/women/11.jpg")
            )
            sampleUsers.forEach { user ->
                userDao.registerUser(user)
            }
        }

        // 2. Tạo Danh mục mẫu nếu chưa có
        val categories = categoryDao.getAllCategories().first()
        if (categories.isEmpty()) {
            val sampleCategories = listOf(
                Category(id = 1, name = "Điện tử", iconUrl = "https://cdn-icons-png.flaticon.com/512/3659/3659899.png"),
                Category(id = 2, name = "Thời trang", iconUrl = "https://cdn-icons-png.flaticon.com/512/3050/3050239.png"),
                Category(id = 3, name = "Nhà cửa", iconUrl = "https://cdn-icons-png.flaticon.com/512/619/619153.png"),
                Category(id = 4, name = "Thể thao", iconUrl = "https://cdn-icons-png.flaticon.com/512/857/857455.png"),
                Category(id = 5, name = "Xe cộ", iconUrl = "https://cdn-icons-png.flaticon.com/512/741/741407.png"),
                Category(id = 6, name = "Sách", iconUrl = "https://cdn-icons-png.flaticon.com/512/3389/3389081.png"),
                Category(id = 7, name = "Tài liệu", iconUrl = "https://cdn-icons-png.flaticon.com/512/2991/2991112.png"),
                Category(id = 8, name = "Mỹ phẩm", iconUrl = "https://cdn-icons-png.flaticon.com/512/3163/3163205.png"),
                Category(id = 9, name = "Phụ kiện", iconUrl = "https://cdn-icons-png.flaticon.com/512/862/862856.png")
            )
            categoryDao.insertCategories(sampleCategories)
        }
    }
}
