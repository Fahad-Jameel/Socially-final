package com.fahad.i210394

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.StoriesDB.StoryDbHelper
import com.fahad.i210394.StoriesDB.StoryRepository
import com.fahad.i210394.apiconfig.apiconf
import com.fahad.i210394.StoryAdapter
import com.fahad.i210394.postdata.Post
import org.json.JSONException
import org.json.JSONObject

class Home : Fragment() {

    private lateinit var dbHelper: StoryDbHelper
    private lateinit var adapter: StoryAdapter
    private lateinit var rvStories: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = StoryDbHelper(requireContext())

        // Kick off the network + DB sync
        val apiUrl = "${apiconf.BASE_URL}Story/getstories.php"
        StoryRepository.syncStories(
            requireContext(),
            apiUrl,
            onSuccess = {
                // Volley callbacks are already on main thread, but safe to do this:
                activity?.runOnUiThread { loadFromDb() }
            },
            onError = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make sure this layout file actually has a RecyclerView with id="rvStories"
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dm = view.findViewById<ImageButton>(R.id.dm);
        dm.setOnClickListener {
            val intent = Intent(requireContext(), DM::class.java)
            startActivity(intent)
        }
        rvStories = view.findViewById(R.id.rvStories)
        rvStories.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        loadFromDb()


        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getPostFromAPI(this, "${apiconf.BASE_URL}Post/getpost.php",
            onSuccess = { postList ->
                val adapter = PostAdapter(requireContext(), postList)
                recyclerView.adapter = adapter
            },
            onFailure = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )


    }

    private fun loadFromDb() {
        val list = dbHelper.getAllStories()
        adapter = StoryAdapter(list) { storyPicBase64 ->
            showStoryDialog(storyPicBase64)
        }
        rvStories.adapter = adapter
    }
    fun getPostFromAPI(
        fragment: Fragment,
        apiUrl: String,
        onSuccess: (List<Post>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val context = fragment.requireContext()
        val queue = Volley.newRequestQueue(context)

        val request = StringRequest(Request.Method.GET, apiUrl, { response ->
            try {
                val json = JSONObject(response)
                val status = json.optString("status")

                if (status == "success") {
                    val postsArray = json.getJSONArray("posts")
                    val postList = mutableListOf<Post>()

                    for (i in 0 until postsArray.length()) {
                        val obj = postsArray.getJSONObject(i)
                        val post = Post(
                            postId = obj.getInt("post_id"),
                            picture = obj.getString("picture"),
                            likes = obj.getInt("likes"),
                            profilePic = obj.getString("profilepic"),
                            caption = obj.getString("caption"),
                            username = obj.getString("username")
                        )
                        postList.add(post)
                    }

                    onSuccess(postList)
                } else {
                    onFailure("API failed: ${json.optString("message")}")
                }
            } catch (e: JSONException) {
                onFailure("JSON Error: ${e.localizedMessage}")
                Log.e("ERr",e.localizedMessage)
            }
        }, { error ->
            onFailure("Network Error: ${error.localizedMessage}")
        })

        queue.add(request)
    }



    private fun showStoryDialog(pictureBase64: String) {
        val bytes = Base64.decode(pictureBase64, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        AlertDialog.Builder(requireContext())
            .setView(
                ImageView(requireContext()).apply {
                    setImageBitmap(bmp)
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setPadding(16, 16, 16, 16)
                }
            )
            .setPositiveButton("Close", null)
            .show()
    }
}
