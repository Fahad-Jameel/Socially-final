package com.fahad.i210394

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import org.json.JSONException
import org.json.JSONObject

class Search : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private val userList = mutableListOf<SearchUser>()
    private lateinit var adapter: SearchUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchInput = view.findViewById(R.id.searchInput)
        recyclerView = view.findViewById(R.id.recyclerSearchUsers)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchUserAdapter(userList)
        recyclerView.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                Log.d("Search_Input", "Keyword: $keyword")

                if (keyword.isNotEmpty()) {
                    fetchUsersFromAPI(keyword)
                } else {
                    userList.clear()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun fetchUsersFromAPI(query: String) {
        val url = "${apiconf.BASE_URL}Search/searchusers.php"
        val queue = Volley.newRequestQueue(requireContext())

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("API_Response", response)
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")

                    if (status == "success") {
                        val profiles = json.getJSONArray("profiles")
                        userList.clear()
                        for (i in 0 until profiles.length()) {
                            val obj = profiles.getJSONObject(i)
                            val user = SearchUser(
                                username = obj.getString("username"),
                                picture = obj.getString("picture")
                            )
                            userList.add(user)
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        userList.clear()
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "No users found.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("API_Error", "JSON Error: ${e.localizedMessage}")
                    Toast.makeText(requireContext(), "JSON Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("API_Error", error.toString())
                Toast.makeText(requireContext(), "Network Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf("query" to query)
            }
        }

        queue.add(request)
    }
}
