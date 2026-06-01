package com.example.rego.data.repository

import com.example.rego.data.model.ChatMessage
import com.example.rego.data.model.UserFirebase
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject
import javax.inject.Singleton

data class Conversation(
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)

@Singleton
class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val chatsRef = database.getReference("chats")
    private val usersRef = database.getReference("users")
    private val userChatsRef = database.getReference("user-chats")

    private val userCache = mutableMapOf<String, UserFirebase>()

    /**
     * Lấy ID phòng chat duy nhất cho 2 người (ghép theo thứ tự bảng chữ cái)
     */
    fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    /**
     * Lấy thông tin User từ Firebase với cache để tăng tốc
     */
    suspend fun getUserInfo(uid: String): UserFirebase? {
        userCache[uid]?.let { return it }
        return try {
            val user = usersRef.child(uid).get().await().getValue(UserFirebase::class.java)
            if (user != null) userCache[uid] = user
            user
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lắng nghe danh sách các cuộc hội thoại của người dùng.
     * Đã tối ưu hóa bằng cách fetch user info song song và sử dụng cache.
     */
    fun listenForConversations(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children.toList()
                
                if (children.isEmpty()) {
                    trySend(emptyList())
                    return
                }

                launch {
                    val conversations = children.map { chatSnapshot ->
                        async {
                            val otherUserId = chatSnapshot.key ?: return@async null
                            val lastMessage = chatSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                            val lastTimestamp = chatSnapshot.child("lastTimestamp").getValue(Long::class.java) ?: 0L

                            val user = getUserInfo(otherUserId)
                            
                            Conversation(
                                otherUserId = otherUserId,
                                otherUserName = user?.name ?: "Người dùng ReGo",
                                otherUserAvatar = user?.avatarUrl ?: "",
                                lastMessage = lastMessage,
                                lastTimestamp = lastTimestamp,
                                isOnline = false
                            )
                        }
                    }.awaitAll().filterNotNull()
                    
                    trySend(conversations.sortedByDescending { it.lastTimestamp })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        val query = userChatsRef.child(currentUserId)
        query.addValueEventListener(listener)

        awaitClose {
            query.removeEventListener(listener)
        }
    }

    /**
     * Lắng nghe tin nhắn từ một phòng chat theo thời gian thực.
     */
    fun listenForMessages(chatRoomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val messagesRef = chatsRef.child(chatRoomId).child("messages")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)

        awaitClose {
            messagesRef.removeEventListener(listener)
        }
    }

    /**
     * Gửi tin nhắn mới.
     */
    suspend fun sendMessage(chatRoomId: String, text: String, senderId: String, receiverId: String) {
        val messagesRef = chatsRef.child(chatRoomId).child("messages")
        val messageId = messagesRef.push().key ?: return
        
        val message = ChatMessage(
            id = messageId,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        messagesRef.child(messageId).setValue(message).await()
        
        // Cập nhật thông tin bổ trợ cho phòng chat
        val chatInfo = mapOf(
            "lastMessage" to text,
            "lastTimestamp" to System.currentTimeMillis(),
            "lastSenderId" to senderId
        )
        chatsRef.child(chatRoomId).updateChildren(chatInfo)

        // Cập nhật danh sách chat cho cả 2 user để hiển thị ở ChatScreen
        userChatsRef.child(senderId).child(receiverId).updateChildren(chatInfo)
        userChatsRef.child(receiverId).child(senderId).updateChildren(chatInfo)
    }
}
