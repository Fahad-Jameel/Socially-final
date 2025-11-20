package com.fahad.i210394

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import kotlinx.coroutines.launch

class Call : AppCompatActivity() {
    private val TAG = "CallActivity"
    private var rtcEngine: RtcEngine? = null
    private var callerId = 0
    private var receiverId = 0
    private var isIncoming = false
    private var callId = 0
    private val channelName = "testchannel"
    private val APP_ID = "fe89e5de87704a8caa08c4f95fc7ee2d"

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Log.d(TAG, "Successfully joined channel: $channel, uid: $uid")
                Toast.makeText(this@Call, "Connected to call", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                Log.d(TAG, "Remote user joined: $uid")
                Toast.makeText(this@Call, "Remote user joined", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Log.d(TAG, "Remote user offline: $uid, reason: $reason")
                Toast.makeText(this@Call, "Remote user left the call", Toast.LENGTH_SHORT).show()
                leaveChannel()
                finish()
            }
        }

        override fun onError(err: Int) {
            runOnUiThread {
                Log.e(TAG, "RTC Error: $err")
                Toast.makeText(this@Call, "Call error: $err", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        enableEdgeToEdge()

        // Get the caller/receiver information
        isIncoming = intent.getBooleanExtra("is_incoming", false)

        if (isIncoming) {
            // This is an incoming call that was accepted
            callerId = intent.getIntExtra("caller_id", 0)
            if (callerId == 0) {
                // If caller_id not provided directly, try from the uid extra
                callerId = (intent.getStringExtra("uid") ?: "0").toInt()
            }
            receiverId = SharedPrefManager.getUserId(this)
            callId = intent.getIntExtra("call_id", 0)
        } else {
            // This is an outgoing call
            callerId = SharedPrefManager.getUserId(this)
            receiverId = (intent.getStringExtra("uid") ?: "0").toInt()
        }

        // Set the name of the other party
        val name = intent.getStringExtra("name") ?: "Unknown"
        findViewById<TextView>(R.id.name).text = name

        // You can also set profile picture if available
        // val profileImageView = findViewById<ImageView>(R.id.profile_image)
        // Load image using your preferred method (Glide, Picasso, etc.)

        Log.d(TAG, "Call activity - Is Incoming: $isIncoming, Caller ID: $callerId, Receiver ID: $receiverId")

        val hangButton = findViewById<ImageView>(R.id.hang)
        hangButton.setOnClickListener {
            Log.d(TAG, "Hang up button clicked")
            leaveChannel()

            // If this is an incoming call, update the status to ended
            if (isIncoming && callId > 0) {
                lifecycleScope.launch {
                    try {
                        NetworkHelper.updateCallStatus(callId, "ended")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating call status to ended", e)
                    }
                }
            }

            finish()
        }

        // Initialize RTC Engine
        initializeRtcEngine()

        // Start or join the call
        if (!isIncoming) {
            // For outgoing calls, notify the server first
            lifecycleScope.launch {
                try {
                    val success = NetworkHelper.startCall(callerId, receiverId)
                    if (success) {
                        Log.d(TAG, "Call started successfully on server")
                        joinChannel()
                    } else {
                        Log.e(TAG, "Failed to start call on server")
                        Toast.makeText(this@Call, "Failed to start call", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting call", e)
                    Toast.makeText(this@Call, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            // For incoming calls, join the channel directly since we've already accepted
            joinChannel()
        }
    }

    private fun initializeRtcEngine() {
        try {
            rtcEngine = RtcEngine.create(applicationContext, APP_ID, rtcEventHandler)
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            rtcEngine?.enableAudio()
            // For audio calls only, disable video
            rtcEngine?.disableVideo()
            Log.d(TAG, "RTC Engine initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RTC Engine", e)
            Toast.makeText(this, "Failed to initialize call engine: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun joinChannel() {
        try {
            // For incoming calls, we join as the receiver ID
            // For outgoing calls, we join as the caller ID
            val myUid = if (isIncoming) receiverId else callerId

            Log.d(TAG, "Joining channel: $channelName as user: $myUid")
            rtcEngine?.joinChannel(null, channelName, null, myUid)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join channel", e)
            Toast.makeText(this, "Failed to join call: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun leaveChannel() {
        Log.d(TAG, "Leaving channel")
        try {
            rtcEngine?.leaveChannel()
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving channel", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }
}