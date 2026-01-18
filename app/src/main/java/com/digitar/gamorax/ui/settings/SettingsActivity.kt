package com.digitar.gamorax.ui.settings

import com.digitar.gamorax.R

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.digitar.gamorax.ui.main.MainActivity
import com.google.android.material.navigation.NavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var authManager: com.digitar.gamorax.data.auth.AuthManager
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        authManager = com.digitar.gamorax.data.auth.AuthManager(this)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Initialize Side Menu (Drawer) logic for Settings screen
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.nav_profile -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
//                R.id.nav_wallet -> Toast.makeText(this, "Wallet clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> drawerLayout.closeDrawer(GravityCompat.START) // Already here
                R.id.nav_logout -> handleLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        setupThemeSelection()
        displayAppVersion()
        setupAccountSettings()
    }

    private fun setupAccountSettings() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val accountCard = findViewById<android.view.View>(R.id.cardAccountSettings)
        val accountHeader = findViewById<android.view.View>(R.id.tvAccountHeader)

        if (user == null) {
            accountCard.visibility = android.view.View.GONE
            accountHeader.visibility = android.view.View.GONE
            return
        }
        
        accountCard.visibility = android.view.View.VISIBLE
        accountHeader.visibility = android.view.View.VISIBLE

        val btnSetPass = findViewById<android.view.View>(R.id.btnSetPassword)
        val divSetPass = findViewById<android.view.View>(R.id.dividerSetPass)
        val btnChangePass = findViewById<android.view.View>(R.id.btnChangePassword)
        val divChangePass = findViewById<android.view.View>(R.id.dividerChangePass)
        val btnDelete = findViewById<android.view.View>(R.id.btnDeleteAccount)
        val btnLogout = findViewById<android.view.View>(R.id.btnLogout)

        // 1. Set Password (only if Google user and NOT Email user yet)
        if (authManager.isGoogleUser() && !authManager.isEmailUser()) {
            btnSetPass.visibility = android.view.View.VISIBLE
            divSetPass.visibility = android.view.View.VISIBLE
            btnSetPass.setOnClickListener { showSetPasswordDialog(user.email ?: "") }
        }

        // 2. Change Password (only if Email user)
        if (authManager.isEmailUser()) {
            btnChangePass.visibility = android.view.View.VISIBLE
            divChangePass.visibility = android.view.View.VISIBLE
            btnChangePass.setOnClickListener { showChangePasswordDialog(user.email ?: "") }
        }

        // 3. Delete Account
        btnDelete.setOnClickListener { showDeleteAccountDialog() }

        // 4. Logout
        btnLogout.setOnClickListener { handleLogout() }
    }

    private fun handleLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                authManager.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSetPasswordDialog(email: String) {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "New Password"
        
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.marginStart = 50; params.marginEnd = 50
        input.layoutParams = params
        container.addView(input)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Set Password")
            .setMessage("Create a password to sign in with Email ($email)")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val pass = input.text.toString()
                if (pass.length < 6) {
                    Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                authManager.linkEmailCredentials(email, pass,
                    onSuccess = { 
                        Toast.makeText(this, "Password set! You can now login with Email.", Toast.LENGTH_LONG).show()
                        recreate() // Refresh UI to show 'Change Password' instead
                    },
                    onFailure = { e -> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog(email: String) {
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val oldPassInput = android.widget.EditText(this)
        oldPassInput.hint = "Current Password"
        oldPassInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        
        val newPassInput = android.widget.EditText(this)
        newPassInput.hint = "New Password"
        newPassInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        
        layout.addView(oldPassInput)
        layout.addView(newPassInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val oldPass = oldPassInput.text.toString()
                val newPass = newPassInput.text.toString()
                
                if (oldPass.isEmpty() || newPass.length < 6) {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                authManager.reauthenticate(email, oldPass,
                    onSuccess = {
                         authManager.updatePassword(newPass,
                            onSuccess = { Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show() },
                            onFailure = { e -> Toast.makeText(this, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onFailure = { Toast.makeText(this, "Wrong current password", Toast.LENGTH_SHORT).show() }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure? This action is IRREVERSIBLE. All your game data will be lost.")
            .setPositiveButton("Delete Forever") { _, _ ->
                 authManager.deleteAccount(
                     onSuccess = {
                         Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show()
                         val intent = Intent(this, MainActivity::class.java)
                         intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                         startActivity(intent)
                     },
                     onFailure = { e -> Toast.makeText(this, "Delete Failed: ${e.message}", Toast.LENGTH_SHORT).show() }
                 )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun setupThemeSelection() {
        val radioGroup = findViewById<RadioGroup>(R.id.themeRadioGroup)
        val savedTheme = ThemeManager.getSavedTheme(this)

        when (savedTheme) {
            ThemeManager.THEME_LIGHT -> radioGroup.check(R.id.radioLight)
            ThemeManager.THEME_DARK -> radioGroup.check(R.id.radioDark)
            else -> radioGroup.check(R.id.radioSystem)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioLight -> ThemeManager.THEME_LIGHT
                R.id.radioDark -> ThemeManager.THEME_DARK
                else -> ThemeManager.THEME_SYSTEM
            }
            ThemeManager.saveTheme(this, mode)
            ThemeManager.applyTheme(mode)
        }
    }

    private fun displayAppVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            findViewById<TextView>(R.id.versionText).text = "App Version $version"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}