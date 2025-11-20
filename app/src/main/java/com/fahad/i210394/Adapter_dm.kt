package com.fahad.i210394

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class Adapter_dm(
    private val context: Context,
    private val userList: List<Model_dm>
) : RecyclerView.Adapter<Adapter_dm.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.name)
        val profileImage: CircleImageView = itemView.findViewById(R.id.imageView)
        val clickButton: LinearLayout = itemView.findViewById(R.id.clickbutto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_dm, parent, false) // your item layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = user.username
        holder.profileImage.setImageBitmap(user.pictureBitmap)

        holder.clickButton.setOnClickListener {
            val intent = Intent(context, DM2::class.java)
            intent.putExtra("id", user.id)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size
}
