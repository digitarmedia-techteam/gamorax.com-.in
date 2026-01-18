package com.digitar.gamorax.authorization

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log // Use Android Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.DialogFragment
import com.digitar.gamorax.R
import com.digitar.gamorax.data.auth.AuthManager
import com.digitar.gamorax.ui.auth.LoginActivity
import com.digitar.gamorax.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton // For Google Button

class LoginPopupDialog : DialogFragment() {

    private lateinit var authManager: AuthManager

    // 1. Activity Result API for Google Sign In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("AUTH_GOOGLE", "Google Sign-In Activity Result OK")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("AUTH_GOOGLE", "Google Sign-In Token Retrieved: ${account.id}")
                
                account.idToken?.let { token ->
                    authenticateWithFirebase(token)
                } ?: run {
                     Log.e("AUTH_GOOGLE", "Google ID Token is NULL")
                     showToast("Google Login Error: Component missing")
                }
            } catch (e: ApiException) {
                Log.e("AUTH_GOOGLE", "Google Sign-In API Exception", e)
                showToast("Google Sign-In Failed: ${e.statusCode}")
            }
        } else {
             Log.d("AUTH_GOOGLE", "Google Sign-In Cancelled or Failed (ResultCode: ${result.resultCode})")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        authManager = AuthManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_authorization, container, false)
    }

    private var isLoginMode = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Close Button
        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            Log.d("UI_POPUP", "Login Popup Dismissed by User")
            dismiss()
        }

        // Guest Login
        view.findViewById<AppCompatButton>(R.id.btnGuest).setOnClickListener {
            handleGuestLogin(view)
        }

        // --- View Switching Logic ---
        val layoutMain = view.findViewById<View>(R.id.layoutMainButtons)
        val layoutEmail = view.findViewById<View>(R.id.layoutEmailForm)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        
        // Email Login Link -> Show Form
        view.findViewById<TextView>(R.id.txtEmailLogin).setOnClickListener {
            layoutMain.visibility = View.GONE
            layoutEmail.visibility = View.VISIBLE
            tvTitle.text = "Welcome Back"
            isLoginMode = true
            updateEmailFormUI(view)
        }

        // Back to Options
        view.findViewById<TextView>(R.id.tvBackToMain).setOnClickListener {
            layoutEmail.visibility = View.GONE
            layoutMain.visibility = View.VISIBLE
            tvTitle.text = "Play 100+ Games Instantly"
        }

        // --- Email Form Logic ---
        val etEmail = view.findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword = view.findViewById<android.widget.EditText>(R.id.etPassword)
        val btnAction = view.findViewById<android.widget.Button>(R.id.btnEmailLogin)
        val tvToggleMode = view.findViewById<TextView>(R.id.tvCreateAccount)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)

        // Toggle Login / Sign Up
        tvToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateEmailFormUI(view)
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.error = "Enter email first"
                return@setOnClickListener
            }
            authManager.sendPasswordResetEmail(email,
                onSuccess = { showToast("Reset link sent to $email") },
                onFailure = { showToast("Failed: ${it.message}") }
            )
        }

        // Main Action Button (Login / Sign Up)
        btnAction.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                showToast("Please fill all fields")
                return@setOnClickListener
            }

            if (isLoginMode) {
                // Login
                btnAction.isEnabled = false
                btnAction.text = "Signing in..."
                authManager.signInWithEmail(email, pass,
                    onSuccess = { 
                        navigateToMain() 
                    },
                    onFailure = { e -> 
                        btnAction.isEnabled = true
                        btnAction.text = "Login"
                        handleAuthError(e)
                    }
                )
            } else {
                // Sign Up
                btnAction.isEnabled = false
                btnAction.text = "Creating Account..."
                authManager.signUpWithEmail(email, pass,
                    onSuccess = { 
                        navigateToMain() 
                    },
                    onFailure = { e -> 
                        btnAction.isEnabled = true
                        btnAction.text = "Create Account"
                        handleAuthError(e) 
                    }
                )
            }
        }

        // Google Login
        view.findViewById<View>(R.id.btnGoogle).setOnClickListener {
            Log.d("AUTH_GOOGLE", "Google Login Button Clicked")
            initiateGoogleLogin()
        }
    }

    private fun updateEmailFormUI(view: View) {
        val btnAction = view.findViewById<android.widget.Button>(R.id.btnEmailLogin)
        val tvToggleMode = view.findViewById<TextView>(R.id.tvCreateAccount)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvForgot = view.findViewById<TextView>(R.id.tvForgotPassword)

        if (isLoginMode) {
            tvTitle.text = "Welcome Back"
            btnAction.text = "Login"
            tvToggleMode.text = "Create Account"
            tvForgot.visibility = View.VISIBLE
        } else {
            tvTitle.text = "Join Gamorax"
            btnAction.text = "Sign Up"
            tvToggleMode.text = "Already have an account? Login"
            tvForgot.visibility = View.INVISIBLE
        }
    }
    
    private fun initiateGoogleLogin() {
        val signInIntent = authManager.getGoogleSignInClient().signInIntent
        Log.d("AUTH_GOOGLE", "Launching Google Sign-In Intent")
        googleSignInLauncher.launch(signInIntent)
    }

    private fun authenticateWithFirebase(idToken: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        
        if (currentUser != null && currentUser.isAnonymous) {
            // Case 1: Guest User is upgrading to Google -> LINK
            Log.d("AUTH_GUEST", "Upgrading Guest to Google Account (Link)")
            authManager.linkWithGoogle(idToken,
                onSuccess = { user ->
                    Log.d("AUTH_LINK", "Guest data merged successfully!")
                    showToast("Account linked! Your guest progress is saved.")
                    navigateToMain()
                },
                onFailure = { e ->
                    Log.e("AUTH_LINK", "Failed to link Guest with Google", e)
                    if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        // Edge Case: Google account already exists on another user
                        showToast("This Google account is already linked to another profile.")
                        // Optional: Prompt user if they want to switch to that account instead
                    } else {
                        showToast("Link Failed: ${e.message}")
                    }
                }
            )
        } else {
            // Case 2: New user or returning Google user -> SIGN IN
            Log.d("AUTH_GOOGLE", "Regular Google Sign-In")
            authManager.signInWithGoogle(idToken,
                onSuccess = { user ->
                    Log.d("AUTH_FIREBASE", "Google Sign-In Success: ${user.username}")
                    navigateToMain()
                },
                onFailure = { e ->
                    Log.e("AUTH_FIREBASE", "Google Sign-In Failed", e)
                    handleAuthError(e)
                }
            )
        }
    }
    
    private fun handleGuestLogin(view: View) {
        // Show loading state if desired
        val btnGuest = view.findViewById<AppCompatButton>(R.id.btnGuest)
        btnGuest.isEnabled = false
        btnGuest.text = "Signing in..."
        
        authManager.signInAsGuest(
            onSuccess = { user ->
                Log.d("AUTH_FIREBASE", "Guest Login Success: ${user.username}")
                showToast("Welcome, ${user.username}!")
                navigateToMain()
            },
            onFailure = { exception ->
                Log.e("AUTH_FIREBASE", "Guest Login Failed", exception)
                showToast("Failed to sign in: ${exception.message}")
                
                // Reset button
                btnGuest.isEnabled = true
                btnGuest.text = "Play as Guest"
            }
        )
    }

    private fun navigateToMain() {
        Log.d("NAVIGATION", "Navigating to Main Screen")
        dismiss()
        
        // Open Main Activity and clear back stack
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // If hosted by LoginActivity, finish it
        activity?.finish()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun handleAuthError(e: Exception) {
        val message = when (e) {
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Account does not exist. Please Create Account."
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "This email is already in use."
            else -> "Authentication Failed: ${e.localizedMessage}"
        }
        showToast(message)
        Log.e("AUTH_ERROR", "Detailed Auth Error", e)
    }
}
