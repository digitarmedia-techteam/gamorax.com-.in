package com.digitar.gamorax.authorization

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.digitar.gamorax.R

class LoginActivity : AppCompatActivity() {

    private lateinit var loginTab: TextView
    private lateinit var signUpTab: TextView
    private lateinit var selectionIndicator: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        loginTab = findViewById(R.id.loginTab)
//        signUpTab = findViewById(R.id.signUpTab)
//        selectionIndicator = findViewById(R.id.selectionIndicator)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, LoginFragment())
            }
        }

//        loginTab.setOnClickListener {
//            if (!supportFragmentManager.findFragmentById(R.id.fragment_container_view)!!
//                    .javaClass.simpleName.equals(LoginFragment::class.java.simpleName, true)) {
//                navigateToLogin()
//                animateSelection(it)
//            }
//        }

//        signUpTab.setOnClickListener {
//            if (!supportFragmentManager.findFragmentById(R.id.fragment_container_view)!!
//                    .javaClass.simpleName.equals(SignupFragment::class.java.simpleName, true)) {
//                navigateToSignUp()
//                animateSelection(it)
//            }
//        }
    }

//    private fun animateSelection(view: View) {
//        val animation = ObjectAnimator.ofFloat(selectionIndicator, "x", view.x)
//        animation.duration = 300
//        animation.start()
//        updateTabColors(view)
//    }

    private fun updateTabColors(selectedTab: View) {
        if (selectedTab.id == R.id.loginTab) {
            loginTab.setTextColor(ContextCompat.getColor(this, R.color.white))
            signUpTab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        } else {
            loginTab.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            signUpTab.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    fun navigateToSignUp() {
        supportFragmentManager.commit {
//            setCustomAnimations(
//                R.animator.card_flip_up_in,
//                R.animator.card_flip_up_out,
//                R.animator.card_flip_down_in,
//                R.animator.card_flip_down_out
//            )
            replace(R.id.fragment_container_view, SignupFragment())
            addToBackStack(null)
        }
    }

    fun navigateToLogin() {
        supportFragmentManager.popBackStack()
    }
}
