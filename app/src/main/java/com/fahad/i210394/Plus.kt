package com.fahad.i210394

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar

class Plus : Fragment() {

    // Track current mode (post or story)
    private var isPostMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Camera button setup
        val cameraButton = view.findViewById<ImageButton>(R.id.cambutton)
        cameraButton.setOnClickListener {
            val intent = Intent(requireContext(), PostCamera::class.java)
            startActivity(intent)
        }

        // Next button setup
        val nextButton = view.findViewById<TextView>(R.id.next)
        nextButton.setOnClickListener {
            if (isPostMode) {
                val intent = Intent(requireContext(), PostComplete::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), Story::class.java)
                startActivity(intent)
            }
        }

        // Set up tab selectors
        val tabPost = view.findViewById<TextView>(R.id.tab_post)
        val tabStory = view.findViewById<TextView>(R.id.tab_story)

        // Post tab click handler
        tabPost.setOnClickListener {
            isPostMode = true
            updateTabAppearance(tabPost, tabStory)
            val intent = Intent(requireContext(), PostCamera::class.java)
            startActivity(intent)
            // You can update preview image or other UI elements specific to post mode here
        }

        // Story tab click handler
        tabStory.setOnClickListener {
            isPostMode = false
            updateTabAppearance(tabStory, tabPost)
            val intent= Intent(requireContext(), Story::class.java)
            startActivity(intent)

            // You can update preview image or other UI elements specific to story mode here
        }
    }

    // Helper method to update the appearance of the tabs
    private fun updateTabAppearance(selectedTab: TextView, unselectedTab: TextView) {
        // Update selected tab
        selectedTab.setTextColor(resources.getColor(R.color.post_yellow, null))
        selectedTab.setBackgroundResource(R.drawable.tab_selected)

        // Update unselected tab
        unselectedTab.setTextColor(resources.getColor(R.color.gray_text, null))
        unselectedTab.setBackgroundResource(R.drawable.tab_unselected)
    }
}