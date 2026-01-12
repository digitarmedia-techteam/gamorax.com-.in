package com.digitar.gamorax.authorization

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.digitar.gamorax.MainActivity
import com.digitar.gamorax.databinding.FragmentLoginBinding
import kotlin.math.max
import kotlin.math.min

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var dX: Float = 0f
    private var initialX: Float = 0f
    private var isSlid = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSwitchToSignup.setOnClickListener {
            (activity as? LoginActivity)?.navigateToSignUp()
        }

        setupSlideButton()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideButton() {
        val track = binding.slideButtonTrack
        val thumb = binding.slideThumb

        track.post {
            thumb.setOnTouchListener { view, event ->
                val maxSlide = track.width - thumb.width - 10 // Padding

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        initialX = view.x
                        true // We are handling the touch event
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!isSlid) {
                            var newX = event.rawX + dX
                            newX = max(initialX, min(newX, (initialX + maxSlide)))
                            view.animate()
                                .x(newX)
                                .setDuration(0)
                                .start()

                            if (newX >= initialX + maxSlide * 0.95) {
                                isSlid = true
                                handleLoginSuccess()
                            }
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (!isSlid) {
                            view.animate()
                                .x(initialX)
                                .setDuration(300)
                                .start()
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun handleLoginSuccess() {
        // To prevent the user from sliding again, you might want to disable the listener.
        binding.slideThumb.setOnTouchListener(null)
        navigateToMain()
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        activity?.finish() // Optional: Finish LoginActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isSlid = false // Reset state
    }
}
