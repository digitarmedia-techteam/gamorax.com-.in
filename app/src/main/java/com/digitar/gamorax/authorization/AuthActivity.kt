package com.digitar.gamorax.authorization

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.digitar.gamorax.R
import com.digitar.gamorax.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    fun navigateToSignUp() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.flip_in_right,
                R.animator.flip_out_left,
                R.animator.flip_in_left,
                R.animator.flip_out_right
            )
            .replace(R.id.fragment_container, SignupFragment())
            .addToBackStack(null)
            .commit()
    }


    fun navigateToLogin() {
        supportFragmentManager.popBackStack()
    }
}