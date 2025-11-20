package com.fahad.i210394

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout

class FollowList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_follow_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, FollowersFragment()).commit()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when (tab?.position) {
                    0 -> FollowersFragment()
                    else -> FollowingFragment()
                }
                supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Set tab names
        tabLayout.addTab(tabLayout.newTab().setText("Followers"))
        tabLayout.addTab(tabLayout.newTab().setText("Following"))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // Close current activity
        return true
    }
}