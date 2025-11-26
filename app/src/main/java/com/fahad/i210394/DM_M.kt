package com.fahad.i210394

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import com.fahad.i210394.profiledb.ProfileDBHelper
import org.json.JSONException
import  com.fahad.i210394.profiledb.Profile
class DM_M : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter_dm
    private val userList = mutableListOf<Model_dm>()

    private lateinit var dbHelper: ProfileDBHelper
    private val apiUrl = "${apiconf.BASE_URL}profile/getallprofiles.php" // Fixed: lowercase 'profile' to match server directory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = ProfileDBHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_d_m__m, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = Adapter_dm(requireContext(), userList)
        recyclerView.adapter = adapter

        loadProfilesFromDB()
        fetchProfilesFromAPI()
    }

    private fun fetchProfilesFromAPI() {
        val queue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response ->
                try {
                    if (response.getString("status") == "success") {
                        val profilesArray = response.getJSONArray("profiles")
                        for (i in 0 until profilesArray.length()) {
                            val obj = profilesArray.getJSONObject(i)
                            val profile = Profile(
                                id = obj.getInt("id"),
                                username = obj.getString("username"),
                                picture = obj.getString("picture")
                            )
                            dbHelper.insertProfile(profile)
                        }
                        context?.let {
                            Toast.makeText(it, "Profiles saved to DB", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        context?.let {
                            Toast.makeText(requireContext(), "No profiles found", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                } catch (e: JSONException) {
                    context?.let {
                        Toast.makeText(
                            requireContext(),
                            "JSON error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            { error ->
                context?.let {
                    Toast.makeText(
                        it,
                        "Error loading profiles: ${error.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("DM_M", "Volley error: ${error.message}", error)
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun loadProfilesFromDB() {
        userList.clear()
        val profiles = dbHelper.getAllProfiles()

        for (profile in profiles) {
            val decodedBytes = Base64.decode(profile.picture, Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            userList.add(Model_dm(profile.id,profile.username, bitmap))
        }

        adapter.notifyDataSetChanged()
    }

}
