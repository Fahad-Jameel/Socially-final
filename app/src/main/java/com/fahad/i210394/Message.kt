package com.fahad.i210394

data class Message(
    val id: Int,
    val senderId: Int,          // Sender ID
    val receiverId: Int,        // Receiver ID
    val content: String,        // Message content (text)
    val messageType: String,    // Message type (text, media, etc.)
    val timestamp: String,      // Timestamp of the message
    val mediaUri: String? = "" , // URI of media (if any)

)

