package com.fahad.i210394

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import com.fahad.i210394.postdata.Comment
import org.json.JSONObject

class CommentsDialog(context: Context, private val postId: Int) : Dialog(context) {

    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentEditText: EditText
    private lateinit var sendCommentButton: Button
    private val comments = mutableListOf<Comment>()
    private lateinit var adapter: CommentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_comments)
        setCancelable(true)

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentEditText = findViewById(R.id.commentEditText)
        sendCommentButton = findViewById(R.id.sendCommentButton)

        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CommentsAdapter(comments)
        commentsRecyclerView.adapter = adapter

        loadComments()

        sendCommentButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            } else {
                Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments() {
        val url = "${apiconf.BASE_URL}Comment/get_comments.php"
        val queue = Volley.newRequestQueue(context)

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        comments.clear()
                        val commentsArray = jsonResponse.getJSONArray("comments")
                        for (i in 0 until commentsArray.length()) {
                            val obj = commentsArray.getJSONObject(i)
                            comments.add(
                                Comment(
                                    commentId = obj.getInt("comment_id"),
                                    commentText = obj.getString("comment_text"),
                                    username = obj.getString("username"),
                                    profilePic = obj.optString("profile_pic", ""),
                                    timestamp = obj.getString("timestamp")
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("CommentsDialog", "Error parsing comments: ${e.message}")
                    Toast.makeText(context, "Error loading comments", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("CommentsDialog", "Error loading comments: ${error.message}")
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("post_id" to postId.toString())
            }
        }

        queue.add(request)
    }

    private fun addComment(commentText: String) {
        val userId = SharedPrefManager.getUserId(context)
        if (userId == -1) {
            Toast.makeText(context, "Please login to comment", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "${apiconf.BASE_URL}Comment/add_comment.php"
        val queue = Volley.newRequestQueue(context)

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        commentEditText.text.clear()
                        loadComments() // Reload comments to show the new one
                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, jsonResponse.optString("message", "Failed to add comment"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("CommentsDialog", "Error parsing response: ${e.message}")
                    Toast.makeText(context, "Error adding comment", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("CommentsDialog", "Error adding comment: ${error.message}")
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "post_id" to postId.toString(),
                    "user_id" to userId.toString(),
                    "comment_text" to commentText
                )
            }
        }

        queue.add(request)
    }
}

