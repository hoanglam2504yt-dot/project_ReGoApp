package com.example.rego.data.repository

import android.net.Uri
import com.example.rego.data.local.dao.UserDao
import com.example.rego.data.local.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun updateProfile(user: User, imageUri: Uri? = null): Result<Unit> {
        return try {
            var finalUser = user
            
            // 1. Upload Avatar if present
            imageUri?.let { uri ->
                // Cost-optimization: Delete old avatar if it exists on Firebase
                user.avatarUrl?.let { oldUrl ->
                    if (oldUrl.contains("firebasestorage")) {
                        try {
                            storage.getReferenceFromUrl(oldUrl).delete().await()
                        } catch (e: Exception) {
                            // Ignore if old file not found
                        }
                    }
                }

                val ref = storage.reference.child("avatars/${user.id}_${System.currentTimeMillis()}.jpg")
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                finalUser = user.copy(avatarUrl = downloadUrl)
            }

            // 2. Update Local Room
            userDao.updateUser(finalUser)

            // 3. Update Firestore
            firestore.collection("users").document(finalUser.id.toString())
                .set(finalUser).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
