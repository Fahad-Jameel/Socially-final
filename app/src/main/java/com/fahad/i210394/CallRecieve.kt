package com.fahad.i210394

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.agora.rtc2.RtcEngine
import kotlinx.coroutines.launch

class CallReceive : AppCompatActivity() {
    private val TAG = "CallReceiveActivity"
    private var callId = 0
    private var callerId = 0
    private var receiverId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_recieve)

        callId = intent.getIntExtra("call_id", 0)
        callerId = intent.getIntExtra("caller_id", 0)
        receiverId = SharedPrefManager.getUserId(this)

        // Fetch caller name from intent or retrieve it
        val callerName = intent.getStringExtra("caller_name") ?: "Unknown Caller"

        Log.d(TAG, "Incoming call - Call ID: $callId, Caller ID: $callerId, Caller Name: $callerName")

        val acceptBtn = findViewById<ImageButton>(R.id.accept_call)
        val rejectBtn = findViewById<ImageButton>(R.id.reject_call)

        acceptBtn.setOnClickListener {
            Log.d(TAG, "Call accepted")
            lifecycleScope.launch {
                try {
                    val success = NetworkHelper.updateCallStatus(callId, "accepted")
                    if (success) {
                        Log.d(TAG, "Call status updated to accepted")

                        // Launch the main Call activity with caller information
                        val callIntent = Intent(this@CallReceive, Call::class.java).apply {
                            putExtra("uid", callerId.toString())
                            putExtra("name", callerName)
                            putExtra("is_incoming", true)
                            putExtra("call_id", callId)
                            // Add flags to clear previous activities if needed
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(callIntent)
                        finish() // Close the receive call screen
                    } else {
                        Log.e(TAG, "Failed to update call status")
                        Toast.makeText(this@CallReceive, "Failed to accept call", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error accepting call", e)
                    Toast.makeText(this@CallReceive, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        rejectBtn.setOnClickListener {
            Log.d(TAG, "Call rejected")
            lifecycleScope.launch {
                try {
                    NetworkHelper.updateCallStatus(callId, "rejected")
                    Log.d(TAG, "Call status updated to rejected")
                } catch (e: Exception) {
                    Log.e(TAG, "Error rejecting call", e)
                }
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "CallReceive activity destroyed")
    }
}