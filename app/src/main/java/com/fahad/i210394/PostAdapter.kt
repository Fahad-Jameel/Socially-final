package com.fahad.i210394

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fahad.i210394.postdata.Post
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class PostAdapter(private val context: Context, private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.profileImage)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val captionText: TextView = view.findViewById(R.id.captionText)
        val likeButton: ToggleButton = view.findViewById(R.id.likeButton)
        val commentButton: ImageView = view.findViewById(R.id.commentButton)
        val shareButton: ImageView = view.findViewById(R.id.shareButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.usernameText.text = post.username
        holder.captionText.text = post.caption

        // Decode and display profile picture
        decodeBase64Image(post.profilePic)?.let { profileBitmap ->
            holder.profileImage.setImageBitmap(profileBitmap)
            holder.postImage.setImageBitmap(profileBitmap)
        } ?: run {
            Log.e("DecodeError", "Failed to decode profile picture")
            holder.profileImage.setImageResource(R.drawable.prf) // Fallback
        }


        decodeBase64Image(post.picture)?.let { postBitmap ->
            holder.postImage.setImageBitmap(postBitmap)
        } ?: run {
            Log.e("DecodeError", "Failed to decode post image: Bitmap is null")
            holder.postImage.setImageResource(R.drawable.post) // Fallback
        }

    }

    override fun getItemCount(): Int = posts.size

    private fun decodeBase64Image(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            Log.d("DecodeError", "Decoded Bytes Length: ${decodedBytes.size}")
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("DecodeError", "Error decoding image: ${e.localizedMessage}")
            null
        }
    }
}
