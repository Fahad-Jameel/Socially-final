package com.fahad.i210394

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.util.Base64
import android.graphics.BitmapFactory

class MessagesAdapter(
    private val messageList: MutableList<Message>,
    private val onMessageLongClick: (Message) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentUserId = SharedPrefManager.getUserId(holder.itemView.context)
        val message = messageList[position]

        if (message.senderId == currentUserId) {
            // Sender
            holder.senderMessageLayout.visibility = View.VISIBLE
            holder.receiverMessageLayout.visibility = View.GONE

            if (message.messageType == "media") {
                holder.senderMessageText.visibility = View.GONE
                holder.senderMessageImage.visibility = View.VISIBLE
                val imageBytes = Base64.decode(message.mediaUri, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.senderMessageImage.setImageBitmap(bitmap)
            } else {
                holder.senderMessageText.visibility = View.VISIBLE
                holder.senderMessageImage.visibility = View.GONE
                holder.senderMessageText.text = message.content
            }

            holder.senderMessageLayout.setOnLongClickListener {
                onMessageLongClick(message)
                true
            }

        } else {
            // Receiver
            holder.receiverMessageLayout.visibility = View.VISIBLE
            holder.senderMessageLayout.visibility = View.GONE

            if (message.messageType == "media") {
                holder.receiverMessageText.visibility = View.GONE
                holder.receiverMessageImage.visibility = View.VISIBLE
                val imageBytes = Base64.decode(message.mediaUri, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.receiverMessageImage.setImageBitmap(bitmap)
            } else {
                holder.receiverMessageText.visibility = View.VISIBLE
                holder.receiverMessageImage.visibility = View.GONE
                holder.receiverMessageText.text = message.content
            }

            holder.receiverMessageLayout.setOnLongClickListener {
                onMessageLongClick(message)
                true
            }
        }
    }

    override fun getItemCount(): Int = messageList.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderMessageLayout: LinearLayout = itemView.findViewById(R.id.sender_message_layout)
        val senderMessageText: TextView = itemView.findViewById(R.id.sender_message_text)
        val senderMessageImage: ImageView = itemView.findViewById(R.id.sender_message_image)
        val receiverMessageLayout: LinearLayout = itemView.findViewById(R.id.receiver_message_layout)
        val receiverMessageText: TextView = itemView.findViewById(R.id.receiver_message_text)
        val receiverMessageImage: ImageView = itemView.findViewById(R.id.receiver_message_image)
    }
}
