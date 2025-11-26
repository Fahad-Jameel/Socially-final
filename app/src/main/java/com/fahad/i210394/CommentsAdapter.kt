package com.fahad.i210394

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fahad.i210394.postdata.Comment
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameText: TextView = view.findViewById(R.id.commentUsername)
        val commentText: TextView = view.findViewById(R.id.commentText)
        val profileImage: CircleImageView = view.findViewById(R.id.commentProfileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.usernameText.text = comment.username
        holder.commentText.text = comment.commentText

        // Decode and display profile picture
        if (comment.profilePic.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(comment.profilePic, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.profileImage.setImageResource(R.drawable.prf)
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.prf)
        }
    }

    override fun getItemCount(): Int = comments.size
}


