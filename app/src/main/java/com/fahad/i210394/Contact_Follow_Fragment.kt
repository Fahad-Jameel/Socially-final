package com.fahad.i210394

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

data class ModelF(
    val id: Int,
    val username: String,
    val picture: Bitmap
)

class Contact_Follow_Fragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val userList = mutableListOf<ModelF>()
    var uid = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact__follow_, container, false)
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
        val url = "${apiconf.BASE_URL}Follow/getcontfoll.php" // Fixed: removed leading slash

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        val usersArray = json.getJSONArray("users")
                        userList.clear()

                        for (i in 0 until usersArray.length()) {
                            val obj = usersArray.getJSONObject(i)
                            val id = obj.getInt("id")
                            val username = obj.getString("username")
                            val base64 = obj.getString("picture")
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                            userList.add(ModelF(id, username, bitmap))
                        }

                        recyclerView.adapter = AdapterF(requireContext(), userList,uid)
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Parsing error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Volley error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["userid"] = userid.toString()
                return params
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }
}

class AdapterF(
    private val context: Context,
    private val userList: List<ModelF>,
    private val myId: Int
) : RecyclerView.Adapter<AdapterF.ViewHolder>() {

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
        val url = "${apiconf.BASE_URL}Follow/follow.php"

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        button.text = "Done"
                        button.isEnabled = false
                        Toast.makeText(context, "Follow request sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, jsonResponse.optString("message", "Failed to send follow request"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["person_id"] = uid.toString()
                params["my_id"] = myId.toString()
                return params
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}
