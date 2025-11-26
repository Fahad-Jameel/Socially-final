// Add these dependencies to your app-level build.gradle file
// implementation 'com.google.firebase:firebase-messaging:23.2.1'
// implementation 'com.google.firebase:firebase-core:21.1.1'

package com.fahad.i210394

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.fahad.i210394.apiconfig.apiconf
import com.fahad.i210394.profiledb.ProfileDBHelper
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class DM2 : AppCompatActivity() {

    private var id = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private val messages = mutableListOf<Message>()
    private var selectedMediaBase64 = ""
    private var screenshotObserver: FileObserver? = null
    private var receiverFcmToken: String = ""
    private var receiverName: String = ""
    var name = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dm2)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        id = intent.getIntExtra("id", -1)

        var base64 = ""


        if (id != -1) {
            val db = ProfileDBHelper(this)
            val user = db.getProfileById(id)
            if (user != null) {
                name = user.username
                base64 = user.picture
                receiverName = name
                fetchReceiverFcmToken(id)
            }
        }

        toolbar.title = name
        val bitmap = if (base64.isNotEmpty()) {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else null
        findViewById<CircleImageView>(R.id.prfp).setImageBitmap(bitmap)

        findViewById<ImageButton>(R.id.callpers).setOnClickListener {
            val intent = Intent(this, Call::class.java)
            intent.putExtra("name", name)
            intent.putExtra("uid", id.toString())
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.upload_button).setOnClickListener {
            openGallery()
        }

        recyclerView = findViewById(R.id.messages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessagesAdapter(messages) { message -> handleLongPressOnMessage(message) }
        recyclerView.adapter = messageAdapter

        loadMessages()

        findViewById<ImageButton>(R.id.send_button).setOnClickListener {
            val messageInput = findViewById<EditText>(R.id.message_input)
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, "text")
                messageInput.text.clear()
            }
        }

        startScreenshotDetection()

        // Get current user's FCM token for reference
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Current user token: $token")
                updateUserFcmToken(token)
            }
        }
    }

    private fun fetchReceiverFcmToken(receiverId: Int) {
        val url = "${apiconf.BASE_URL}Notification/get_fcm_token.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        receiverFcmToken = jsonResponse.getString("fcm_token")
                        Log.d("FCM", "Receiver token: $receiverFcmToken")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Error parsing token: ${e.message}")
                }
            },
            { error ->
                Log.e("FCM", "Error fetching token: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to receiverId.toString())
            }
        }

        queue.add(request)
    }

    private fun updateUserFcmToken(token: String) {
        val senderId = SharedPrefManager.getUserId(this)
        if (senderId == -1) return

        val url = "${apiconf.BASE_URL}Notification/update_fcm_token.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("FCM", "Token updated: $response")
            },
            { error ->
                Log.e("FCM", "Token update error: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to senderId.toString(),
                    "fcm_token" to token
                )
            }
        }

        queue.add(request)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        screenshotObserver?.stopWatching()
    }

    private fun startScreenshotDetection() {
        val screenshotsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Screenshots"
        screenshotObserver = object : FileObserver(screenshotsPath, CREATE) {
            override fun onEvent(event: Int, path: String?) {
                if (event == CREATE && path != null) {
                    runOnUiThread {
                        Toast.makeText(this@DM2, "⚠️ Screenshot taken!", Toast.LENGTH_SHORT).show()
                        sendMessage("⚠️ The user took a screenshot of the chat.", "text")
                    }
                }
            }
        }
        try {
            screenshotObserver?.startWatching()
        } catch (e: Exception) {
            Log.e("ScreenshotObserver", "Error: ${e.message}")
        }
    }

    private fun sendMessage(content: String, type: String) {
        val senderId = SharedPrefManager.getUserId(this)
        if (senderId == -1) return

        val url = if (type == "media") {
            "${apiconf.BASE_URL}Message/send_media_message.php"
        } else {
            "${apiconf.BASE_URL}Message/send_message.php"
        }

        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("DM2", "Sent: $response")

                // After sending message successfully, send notification
                if (receiverFcmToken.isNotEmpty()) {
                    sendPushNotification(content, type)
                }

                loadMessages()
                selectedMediaBase64 = ""
            },
            { error ->
                Log.e("DM2", "Send error: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                return if (type == "media") {
                    hashMapOf(
                        "sender_id" to senderId.toString(),
                        "receiver_id" to id.toString(),
                        "media_uri" to selectedMediaBase64
                    )
                } else {
                    hashMapOf(
                        "sender_id" to senderId.toString(),
                        "receiver_id" to id.toString(),
                        "message_type" to "text",
                        "message_content" to content
                    )
                }
            }
        }

        queue.add(request)
    }

    private fun sendPushNotification(content: String, type: String) {
        val url = "${apiconf.BASE_URL}Notification/send_notification.php"
        val queue = Volley.newRequestQueue(this)

        val senderName = name ?: "Someone"
        val messagePreview = if (type == "media") "📷 [Photo]" else
            if (content.length > 30) content.substring(0, 27) + "..." else content

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("FCM", "Notification sent: $response")
            },
            { error ->
                Log.e("FCM", "Notification error: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "token" to receiverFcmToken,
                    "title" to senderName,
                    "body" to messagePreview,
                    "user_id" to id.toString(),
                    "sender_id" to SharedPrefManager.getUserId(this@DM2).toString(),
                    "notification_type" to "message"
                )
            }
        }

        queue.add(request)
    }

    private fun loadMessages() {
        val senderId = SharedPrefManager.getUserId(this)
        if (senderId == -1) return

        val url = "${apiconf.BASE_URL}Message/get_messages.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getString("status") == "success") {
                    val newMessages = parseMessages(response)
                    messages.clear()
                    messages.addAll(newMessages)
                    messageAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            },
            { error ->
                Log.e("DM2", "Load error: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user1" to senderId.toString(), "user2" to id.toString())
            }
        }

        queue.add(request)
    }

    private fun parseMessages(response: String): List<Message> {
        val list = mutableListOf<Message>()
        val json = JSONObject(response)
        val array = json.getJSONArray("messages")
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Message(
                    obj.getInt("id"),
                    obj.getInt("sender_id"),
                    obj.getInt("receiver_id"),
                    obj.getString("message_content"),
                    obj.getString("message_type"),
                    obj.getString("timestamp"),
                    obj.optString("media_uri", "")
                )
            )
        }
        return list
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        openMediaResultLauncher.launch(intent)
    }

    private val openMediaResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    val bitmap = contentResolver.openInputStream(it)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                    bitmap?.let { bmp ->
                        selectedMediaBase64 = encodeToBase64(bmp)
                        sendMessage("[Media]", "media")
                    }
                }
            }
        }

    private fun encodeToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val bytes = baos.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun handleLongPressOnMessage(message: Message) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditMessageDialog(message)
                    1 -> deleteMessage(message)
                }
            }
            .show()
    }

    private fun showEditMessageDialog(message: Message) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_message, null)
        val editText = view.findViewById<EditText>(R.id.edit_message_input)
        editText.setText(message.content)

        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val newContent = editText.text.toString()
                if (newContent.isNotEmpty()) editMessage(message, newContent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editMessage(message: Message, newContent: String) {
        val url = "${apiconf.BASE_URL}Message/edit_message.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            { loadMessages() },
            { error -> Log.e("DM2", "Edit error: ${error.message}") }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("message_id" to message.id.toString(), "new_content" to newContent)
            }
        }

        queue.add(request)
    }

    private fun deleteMessage(message: Message) {
        val url = "${apiconf.BASE_URL}Message/delete_message.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(Method.POST, url,
            { loadMessages() },
            { error -> Log.e("DM2", "Delete error: ${error.message}") }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("message_id" to message.id.toString())
            }
        }

        queue.add(request)
    }
}