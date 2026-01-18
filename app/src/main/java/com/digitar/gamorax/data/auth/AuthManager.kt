package com.digitar.gamorax.data.auth

import android.content.Context
import android.util.Log
import com.digitar.gamorax.R
import com.digitar.gamorax.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("gamorax_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG_GOOGLE = "AUTH_GOOGLE"
        private const val TAG_FIREBASE = "AUTH_FIREBASE"
        private const val TAG_FIRESTORE = "FIRESTORE_USER"
        private const val TAG_LINK = "AUTH_LINK"
        private const val TAG_PASSWORD = "AUTH_PASSWORD"
    }

    // Google Sign In Client
    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    // Authenticate with Firebase using Google ID Token
    fun signInWithGoogle(
        idToken: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG_FIREBASE, "Starting Firebase credential creation with Google token")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                Log.d(TAG_FIREBASE, "Firebase Google Sign-In Successful: ${firebaseUser?.uid}")

                if (firebaseUser != null) {
                    val user = User(
                        userId = firebaseUser.uid,
                        username = firebaseUser.displayName ?: "Gamer",
                        email = firebaseUser.email ?: "",
                        profileImage = firebaseUser.photoUrl?.toString() ?: "",
                        bio = "Elite Warrior",
                        isGuest = false
                    )
                    saveUserToFirestore(user, onSuccess, onFailure)
                } else {
                    onFailure(Exception("Firebase User is null"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_FIREBASE, "Firebase Google Sign-In Failed", e)
                onFailure(e)
            }
    }

    // Link Google Credential to current user
    fun linkWithGoogle(
        idToken: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            signInWithGoogle(idToken, onSuccess, onFailure)
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        currentUser.linkWithCredential(credential)
            .addOnSuccessListener {
                Log.d(TAG_LINK, "Linked with Google successfully")
                val user = User(
                    userId = currentUser.uid,
                    username = currentUser.displayName ?: "Gamer",
                    email = currentUser.email ?: "",
                    profileImage = currentUser.photoUrl?.toString() ?: "",
                    isGuest = false
                )
                saveUserToFirestore(user, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                Log.e(TAG_LINK, "Link with Google Failed", e)
                onFailure(e)
            }
    }

    // Sign in as guest
    fun signInAsGuest(
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInAnonymously()
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                val username = generateGuestUsername()

                val user = User(
                    userId = userId,
                    username = username,
                    bio = "New player exploring Gamorax!",
                    profileImage = "", // Default handled in UI
                    isGuest = true,
                    createdAt = System.currentTimeMillis()
                )
                saveUserToFirestore(user, onSuccess, onFailure)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG_FIREBASE, "Guest Login Failed", exception)
                onFailure(exception)
            }
    }

    // Email Sign Up
    fun signUpWithEmail(
        email: String,
        pass: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(
                        userId = firebaseUser.uid,
                        username = email.substringBefore("@"),
                        email = email,
                        profileImage = "",
                        isGuest = false
                    )
                    saveUserToFirestore(user, onSuccess, onFailure)
                } else {
                    onFailure(Exception("User creation failed"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG_GOOGLE, "Email Sign Up Failed", e)
                onFailure(e)
            }
    }

    // Email Login
    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                getUserData(uid, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                Log.e(TAG_GOOGLE, "Email Sign In Failed", e)
                onFailure(e)
            }
    }

    // Link Email & Password
    fun linkEmailCredentials(
        email: String,
        pass: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser ?: run {
             onFailure(Exception("No user logged in to link"))
             return
        }

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, pass)
        currentUser.linkWithCredential(credential)
            .addOnSuccessListener {
                Log.d(TAG_LINK, "Linked with Email/Pass successfully")
                val user = User(
                    userId = currentUser.uid,
                    username = currentUser.displayName ?: email.substringBefore("@"),
                    email = email,
                    profileImage = currentUser.photoUrl?.toString() ?: "", 
                    isGuest = false
                )
                saveUserToFirestore(user, onSuccess, onFailure)
            }
            .addOnFailureListener { e ->
                Log.e(TAG_LINK, "Link with Email Failed", e)
                onFailure(e)
            }
    }
    
    // Forgot Password
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Update Password
    fun updatePassword(
        newPass: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPass)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        } else {
            onFailure(Exception("No user logged in"))
        }
    }
    
    // Re-authenticate
    fun reauthenticate(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, pass)
        user?.reauthenticate(credential)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { onFailure(it) }
    }

    // Helpers
    fun isGoogleUser(): Boolean {
        val user = auth.currentUser ?: return false
        for (profile in user.providerData) {
            if (GoogleAuthProvider.PROVIDER_ID == profile.providerId) return true
        }
        return false
    }

    fun isEmailUser(): Boolean {
        val user = auth.currentUser ?: return false
        for (profile in user.providerData) {
            if (com.google.firebase.auth.EmailAuthProvider.PROVIDER_ID == profile.providerId) return true
        }
        return false
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isGuestUser(): Boolean = prefs.getBoolean("is_guest", false)

    fun signOut() {
        auth.signOut()
        getGoogleSignInClient().signOut()
        prefs.edit().apply {
            remove("is_guest")
            remove("user_id")
            apply()
        }
    }
    
    fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).delete()
        user.delete()
            .addOnSuccessListener { 
                prefs.edit().clear().apply()
                onSuccess() 
            }
            .addOnFailureListener { onFailure(it) }
    }

    // Private helpers
    private fun generateGuestUsername(): String {
        val prefixes = listOf("Player", "Guest", "Gamer", "Ninja", "Warrior", "Legend")
        val randomPrefix = prefixes[Random.nextInt(prefixes.size)]
        val randomNumber = Random.nextInt(1000, 9999)
        return "${randomPrefix}_$randomNumber"
    }

    private fun saveUserToFirestore(
        user: User,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userData = hashMapOf<String, Any>(
            "userId" to user.userId,
            "username" to user.username,
            "email" to user.email,
            "profileImage" to user.profileImage,
            "isGuest" to user.isGuest
        )
        if (user.bio.isNotEmpty() && user.bio != "Elite Warrior") {
             userData["bio"] = user.bio
        }

        firestore.collection("users")
            .document(user.userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                prefs.edit().apply {
                    putBoolean("is_guest", user.isGuest)
                    putString("user_id", user.userId)
                    apply()
                }
                onSuccess(user)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getUserData(
        userId: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) onSuccess(user) else onFailure(Exception("Failed to parse user data"))
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

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
        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
