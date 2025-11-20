package com.fahad.i210394

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.fahad.i210394.apiconfig.apiconf
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "chat_messages"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: "New message"
            val body = remoteMessage.data["body"] ?: "You have a new message"
            val senderId = remoteMessage.data["sender_id"]?.toIntOrNull() ?: -1
            val notificationType = remoteMessage.data["notification_type"] ?: "message"

            if (notificationType == "message" && senderId != -1) {
                sendNotification(title, body, senderId)
            }
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            val senderId = remoteMessage.data["sender_id"]?.toIntOrNull() ?: -1
            sendNotification(it.title ?: "New message", it.body ?: "You have a new message", senderId)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = SharedPrefManager.getUserId(this)
        if (userId == -1) return

        val url = "${apiconf.BASE_URL}/Notification/update_fcm_token.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d(TAG, "Token update response: $response")
            },
            { error ->
                Log.e(TAG, "Token update error: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userId.toString(),
                    "fcm_token" to token
                )
            }
        }

        queue.add(request)
    }

    private fun sendNotification(title: String, messageBody: String, senderId: Int) {
        val intent = Intent(this, DM2::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("id", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // Make sure you have this icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android Oreo, notification channels are required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat message notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(senderId, notificationBuilder.build())
    }
}