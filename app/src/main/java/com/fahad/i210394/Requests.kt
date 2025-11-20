package com.fahad.i210394

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

data class ModelR(
    val id: Int,
    val username: String,
    val picture: Bitmap
)
class Requests : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val userList = mutableListOf<ModelR>()
    var uid = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Replace 1 with your actual user ID
        uid = SharedPrefManager.getUserId(requireContext());
        fetchUsersFromAPI(uid)
    }

    private fun fetchUsersFromAPI(userid: Int) {
        val url = apiconf.BASE_URL + "Follow/requests.php" // replace with your real URL

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    // Log the response to check the actual structure
                    Log.d("API Response", response)

                    val json = JSONObject(response)

                    // Check if the 'status' is 'success' before processing
                    if (json.getString("status") == "success") {
                        // Check if 'users' array is present and contains data
                        if (json.has("users") && json.getJSONArray("users").length() > 0) {
                            val usersArray = json.getJSONArray("users")
                            userList.clear()

                            for (i in 0 until usersArray.length()) {
                                val obj = usersArray.getJSONObject(i)
                                val id = obj.getInt("id")
                                val username = obj.getString("username")
                                val base64 = obj.getString("picture")
                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                userList.add(ModelR(id, username, bitmap))
                            }

                            // Set the adapter after fetching data
                            recyclerView.adapter = AdapterR(requireContext(), userList, uid)
                        } else {
                            // Handle case when users array is empty or not present
                            Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Log the exception for easier debugging
                    Log.e("Parsing error", e.message ?: "Unknown error")
                    Toast.makeText(requireContext(), "Parsing error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Volley error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["my_id"] = userid.toString()
                return params
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }


}

class AdapterR(
    private val context: Context,
    private val userList: List<ModelR>,
    private val myId: Int
) : RecyclerView.Adapter<AdapterR.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val profileImage: CircleImageView = view.findViewById(R.id.imageView)
        val inviteButton: Button = view.findViewById(R.id.invite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cont_fol, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.name.text = user.username
        holder.profileImage.setImageBitmap(user.picture)

        holder.inviteButton.setOnClickListener {
            sendUID(user.id, holder.inviteButton)
        }
    }

    override fun getItemCount(): Int = userList.size

    private fun sendUID(uid: Int, button: Button) {
        val url = apiconf.BASE_URL+"Follow/follow.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                button.text = "Done"
                button.isEnabled = false
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["uid"] = uid.toString()
                params["my_id"] = myId.toString()
                return params
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}
