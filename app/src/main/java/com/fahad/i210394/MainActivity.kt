package com.fahad.i210394

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var callListener: CallListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show splash screen for 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000) // 2 seconds delay
    }

    private fun navigateToNextScreen() {
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId != -1) {
            Log.d(TAG, "User already logged in with ID: $userId")
            // Initialize call listener here to ensure it's created for logged-in users
            callListener = CallListener(applicationContext, userId)

            startActivity(Intent(this, Navigation::class.java))
            finish()
        } else {
            Log.d(TAG, "No user logged in, going to login screen")
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM_TOKEN_SUCCESS", "Token: $token")
                } else {
                    Log.e("FCM_TOKEN_FAILED", "Failed to get token", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FCM_TOKEN_ERROR", "Exception getting token", exception)
            }
        initializeFcmToken()
    }

    private fun initializeFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            // Save token locally if needed
            // SharedPrefManager.saveFcmToken(this, token)

            // Send token to your server
            updateTokenOnServer(token)
        }
    }

    private fun updateTokenOnServer(token: String) {
        val userId = SharedPrefManager.getUserId(this)
        if (userId == -1) {
            Log.d("FCM", "User not logged in, can't update token")
            return
        }

        val url = "${apiconf.BASE_URL}Notification/update_fcm_token.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d("FCM", "Token updated successfully: $response")
            },
            { error ->
                Log.e("FCM", "Error updating token: ${error.message}")
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

}

