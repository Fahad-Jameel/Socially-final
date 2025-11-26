package com.fahad.i210394

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.FirebaseApp
import com.fahad.i210394.apiconfig.apiconf
import org.json.JSONObject

class MyApp:Application(),LifecycleObserver {
    private lateinit var callListener: CallListener

    override fun onCreate() {
        super.onCreate()
        val userId = SharedPrefManager.getUserId(this)
        if (userId > 0) {
            Log.d("MyApplication", "Initializing call listener for user: $userId")
            callListener = CallListener(applicationContext, userId)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        FirebaseApp.initializeApp(this)
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        // App comes to foreground — user is onlinep
        updateUserStatus("online")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        // App goes to background — user is offline
        updateUserStatus("offline")
    }

    private fun updateUserStatus(status: String) {
        // Get user ID from SharedPrefManager
        val userId = SharedPrefManager.getUserId(this)

        // Create request URL
        val url = "${apiconf.BASE_URL}setstatus.php"

        // Create request params
        val params = HashMap<String, String>()
        params["id"] = userId.toString()
        params["status"] = status

        // Make POST request using Volley
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    // Parse response JSON
                    val jsonResponse = JSONObject(response)
                    val responseStatus = jsonResponse.getString("status")

                    if (responseStatus == "success") {
                        Log.d("MyApp", "Status updated successfully: $status")
                    } else {
                        Log.e("MyApp", "Failed to update status: ${jsonResponse.getString("message")}")
                    }
                } catch (e: Exception) {
                    Log.e("MyApp", "Error parsing response: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("MyApp", "Volley error: ${error.message}")
            }) {

            override fun getParams(): Map<String, String> {
                return params
            }
        }

        // Add the request to RequestQueue
        Volley.newRequestQueue(applicationContext).add(request)
    }
}