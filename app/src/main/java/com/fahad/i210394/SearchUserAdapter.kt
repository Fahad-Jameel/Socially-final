package com.fahad.i210394

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SearchUserAdapter(private val userList: List<SearchUser>) :
    RecyclerView.Adapter<SearchUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val inviteBtn: Button = view.findViewById(R.id.inviteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_search_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.username

        // Decode and set base64 image
        val pictureData = user.picture
        if (!pictureData.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(pictureData, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.userImage.setImageBitmap(bmp)
            } catch (e: Exception) {
                holder.userImage.setImageResource(R.drawable.default_profile)
            }
        } else {
            holder.userImage.setImageResource(R.drawable.default_profile)
        }

        holder.inviteBtn.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Invited ${user.username}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = userList.size
}
