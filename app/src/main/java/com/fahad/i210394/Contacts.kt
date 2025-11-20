package com.fahad.i210394

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout


class Contacts : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize TabLayout
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        // Load the default fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, FollowersFragment())
            .commit()

        // Handle Tab Selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment = when (tab?.position) {
                    0 -> Contact_All_Fragment()
                    else -> Contact_Follow_Fragment()
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Set tab names
        tabLayout.addTab(tabLayout.newTab().setText("In Contact"))
        tabLayout.addTab(tabLayout.newTab().setText("Invite"))
    }


}