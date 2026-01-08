package com.digitar.gamorax.authorization

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.digitar.gamorax.R

class LoginPopupDialog : DialogFragment() {

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
            dismiss()
        }

        view.findViewById<AppCompatButton>(R.id.btnGuest).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.txtEmailLogin).setOnClickListener {
            startActivity(Intent(requireContext(), loginActivity::class.java))
            dismiss()
        }

        // Add other click listeners for Google/Apple if needed
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            
            // Optional: Add a smooth animation
            setWindowAnimations(android.R.style.Animation_Dialog)
        }
    }
}