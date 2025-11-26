package com.fahad.i210394

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import com.fahad.i210394.profiledb.ProfileDBHelper
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject


class Contact_All_Fragment : Fragment() {

    private val userList = mutableListOf<Model_dm>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: ProfileDBHelper
    private lateinit var adapter: Adapter_cont

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = ProfileDBHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact__all_, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = Adapter_cont(requireContext(), userList)
        recyclerView.adapter = adapter

        loadProfilesFromDB()

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


class Adapter_cont(
    private val context: Context,
    private val userList: List<Model_dm>
) : RecyclerView.Adapter<Adapter_cont.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.name)
        val profileImage: CircleImageView = itemView.findViewById(R.id.imageView)
        val statusIndicator: View = itemView.findViewById(R.id.statusshow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_followers, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = user.username
        holder.profileImage.setImageBitmap(user.pictureBitmap)

        // Set default status color (gray)
        holder.statusIndicator.setBackgroundColor(android.graphics.Color.parseColor("#5c5c5c"))

        // Check user status via API
        checkUserStatus(user.id, holder.statusIndicator)
    }

    override fun getItemCount(): Int = userList.size

    private fun checkUserStatus(userId: Int, statusView: View) {
        // Create request URL
        val url = "${apiconf.BASE_URL}getstatus.php?id=$userId"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val userStatus = jsonResponse.getString("user_status")

                        // Update status indicator color based on status
                        when (userStatus) {
                            "online" -> statusView.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                            "offline" -> statusView.setBackgroundColor(Color.parseColor("#5c5c5c")) // Gray
                        }
                    } else {
                        // Keep default color if status check failed
                        Log.e("StatusCheck", "Failed to get status: ${jsonResponse.optString("message", "Unknown error")}")
                    }
                } catch (e: Exception) {
                    Log.e("StatusCheck", "Error parsing response: ${e.message}")
                }
            },
            { error ->
                Log.e("StatusCheck", "Volley error: ${error.message}")
            }
        )

        // Add the request to RequestQueue
        Volley.newRequestQueue(context).add(request)
    }
}
