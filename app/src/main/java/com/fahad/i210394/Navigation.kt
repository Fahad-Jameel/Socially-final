package com.fahad.i210394

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Navigation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Home())
                .commit()
        }

        // Setup Bottom Navigation View
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.nav_home -> Home()
                R.id.nav_search -> Search()
                R.id.nav_profile -> Profile()
                R.id.nav_plus -> Plus()
                R.id.nav_contacts -> Contacts()
                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
                true
            } ?: false
        }
    }
}