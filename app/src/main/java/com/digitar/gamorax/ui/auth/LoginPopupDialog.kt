package com.digitar.gamorax.authorization

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.DialogFragment
import com.digitar.gamorax.R
import com.digitar.gamorax.ui.auth.LoginActivity
import org.chromium.base.Log

class LoginPopupDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply the full-screen dialog theme
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_authorization, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            // Use dismiss() to trigger the exit animation
            dismiss()
        }

        view.findViewById<AppCompatButton>(R.id.btnGuest).setOnClickListener {
            handleGuestLogin()
        }

        view.findViewById<TextView>(R.id.txtEmailLogin).setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                requireContext(),
                R.animator.card_flip_up_in,
                R.animator.card_flip_up_out
            )
            startActivity(intent, options.toBundle())
            dismiss()
        }


        // Add other click listeners for Google/Apple if needed
    }
    
    private fun handleGuestLogin() {
        val guestAuthManager = com.digitar.gamorax.data.auth.GuestAuthManager(requireContext())
        
        // Show loading (optional - you can add a ProgressBar)
        view?.findViewById<AppCompatButton>(R.id.btnGuest)?.apply {
            isEnabled = false
            text = "Signing in..."
        }
        
        guestAuthManager.signInAsGuest(
            onSuccess = { user ->
                // Success - dismiss dialog and continue to app
                android.widget.Toast.makeText(
                    requireContext(),
                    "Welcome, ${user.username}!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                dismiss()
            },
            onFailure = { exception ->
                Log.e("LOGIN_ERROR", "Failed to sign in", exception)
                // Error handling
                android.widget.Toast.makeText(
                    requireContext(),
                    "Failed to sign in: ${exception.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                // Re-enable button
                view?.findViewById<AppCompatButton>(R.id.btnGuest)?.apply {
                    isEnabled = true
                    text = "Continue as Guest"
                }
            }
        )
    }

    // onStart() is no longer needed as styling is handled by the theme
}
