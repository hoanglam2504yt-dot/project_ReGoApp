package com.example.rego.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rego.data.model.ChatMessage
import com.example.rego.data.model.UserFirebase
import com.example.rego.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _receiverUser = MutableStateFlow<UserFirebase?>(null)
    val receiverUser: StateFlow<UserFirebase?> = _receiverUser.asStateFlow()

    private var currentChatRoomId: String? = null

    /**
     * Khởi tạo phòng chat và lắng nghe tin nhắn thực tế.
     */
    fun setupChat(currentUserId: String, receiverId: String) {
        val roomId = repository.getChatRoomId(currentUserId, receiverId)
        if (currentChatRoomId == roomId) return
        currentChatRoomId = roomId
        
        // 1. Lấy thông tin người nhận để hiển thị lên UI
        viewModelScope.launch {
            _receiverUser.value = repository.getUserInfo(receiverId)
        }

        // 2. Lắng nghe tin nhắn real-time
        viewModelScope.launch {
            repository.listenForMessages(roomId)
                .collect { messages ->
                    _messages.value = messages.sortedBy { it.timestamp }
                }
        }
    }

    /**
     * Gửi tin nhắn thực tế lên Firebase
     */
    fun sendMessage(text: String, senderId: String, receiverId: String) {
        val roomId = currentChatRoomId ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                repository.sendMessage(roomId, text, senderId, receiverId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setChatRoom(chatId: String) {}
}
