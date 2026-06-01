package com.example.rego.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
) {
    constructor() : this("", "", "", "", 0L)
}

data class UserFirebase(
    val uid: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val email: String = ""
) {
    constructor() : this("", "", "", "")
}
