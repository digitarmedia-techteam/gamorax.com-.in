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
            dismiss()
        }

        view.findViewById<TextView>(R.id.txtEmailLogin).setOnClickListener {            val intent = Intent(requireContext(), LoginActivity::class.java)
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
    
    // onStart() is no longer needed as styling is handled by the theme
}
