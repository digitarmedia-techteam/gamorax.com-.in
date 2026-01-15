package com.digitar.gamorax.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.digitar.gamorax.data.model.User
import kotlin.random.Random

class GuestAuthManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("gamorax_prefs", Context.MODE_PRIVATE)

    /**
     * Generate a random username for guest users
     */
    fun generateGuestUsername(): String {
        val prefixes = listOf("Player", "Guest", "Gamer", "Ninja", "Warrior", "Legend")
        val randomPrefix = prefixes[Random.nextInt(prefixes.size)]
        val randomNumber = Random.nextInt(1000, 9999)
        return "${randomPrefix}_$randomNumber"
    }

    /**
     * Sign in as guest using Firebase Anonymous Authentication
     */
    fun signInAsGuest(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInAnonymously()
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                val username = generateGuestUsername()

                // Create user data
                val user = User(
                    userId = userId,
                    username = username,
                    bio = "New player exploring Gamorax!",
                    profileImage = "default_avatar",
                    isGuest = true,
                    createdAt = System.currentTimeMillis()
                )

                // Save to Firestore
                saveUserToFirestore(user, onSuccess, onFailure)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Save user data to Firestore
     */
    private fun saveUserToFirestore(
        user: User,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(user.userId)
            .set(user)
            .addOnSuccessListener {
                // Save guest status locally
                prefs.edit().putBoolean("is_guest", true).apply()
                prefs.edit().putString("user_id", user.userId).apply()
                onSuccess(user)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Update user profile (username and bio)
     */
    fun updateUserProfile(
        userId: String,
        username: String,
        bio: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "username" to username,
            "bio" to bio
        )

        firestore.collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Get user data from Firestore
     */
    fun getUserData(
        userId: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure(Exception("User data not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Check if user is already logged in (guest or regular)
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Check if current user is guest
     */
    fun isGuestUser(): Boolean {
        return prefs.getBoolean("is_guest", false)
    }

    /**
     * Sign out user
     */
    fun signOut() {
        auth.signOut()
        prefs.edit().clear().apply()
    }
}
