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

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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
                R.id.nav_logout -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        setupThemeSelection()
        displayAppVersion()
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