package com.fahad.i210394

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayInputStream


class Profile : Fragment() {
    private var userID = 0
    private lateinit var imageView: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userID = SharedPrefManager.getUserId(requireContext())
        imageView = view.findViewById(R.id.prfpic)
        val button = view.findViewById<ImageButton>(R.id.editpr)
        button.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)
        }

        val button2 = view.findViewById<LinearLayout>(R.id.follscr)
        button2.setOnClickListener{
            val intent = Intent(requireContext(), FollowList::class.java)
            startActivity(intent)
        }

        val logout = view.findViewById<ImageButton>(R.id.logout)

        logout.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                remove("user_id")
                apply()
            }

            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish() // <-- Correct Implementation
        }

        Geting(view)

    }

    private fun Geting(view: View) {
        val url = "${apiconf.BASE_URL}profile/getProfile.php"
        val name = view.findViewById<TextView>(R.id.nametit)

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getString("status") == "success"
                    if (success) {
                        val profile = jsonObject.getJSONObject("profile")
                        val bioText = profile.getString("bio")
                        val usernameText = profile.getString("username")
                        val pictureBase64 = profile.getString("picture")

                        val bioField = view.findViewById<TextView>(R.id.bio)
                        bioField.text = bioText
                        name.text = usernameText

                        // Decode and display the picture
                        val imageBytes = Base64.decode(pictureBase64, Base64.DEFAULT)
                        val inputStream = ByteArrayInputStream(imageBytes)
                        val bitmap = BitmapFactory.decodeStream(inputStream)

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(requireContext(), "Failed to decode image", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "error is: $e")
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userID.toString()
                return params
            }
        }

        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }


}