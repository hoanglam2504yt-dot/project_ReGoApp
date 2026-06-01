package com.example.rego.utils

object ChatUtils {
    /**
     * Tạo ChatRoomId duy nhất giữa 2 user bằng cách nối ID theo thứ tự Alphabet.
     * Đảm bảo dù ai là người nhắn trước, ID phòng chat vẫn luôn là duy nhất.
     */
    fun generateChatRoomId(userId1: String, userId2: String): String {
        val id1 = userId1.trim()
        val id2 = userId2.trim()
        return if (id1 < id2) "${id1}_${id2}" else "${id2}_${id1}"
    }
}
