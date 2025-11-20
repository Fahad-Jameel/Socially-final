package com.fahad.i210394

data class MessageModel(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val messageType: String,
    val messageContent: String,
    val timestamp: String,
    val seen: Boolean
)
