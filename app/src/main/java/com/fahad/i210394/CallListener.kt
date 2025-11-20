package com.fahad.i210394

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*

class CallListener(private val context: Context, private val receiverId: Int) : DefaultLifecycleObserver {
    private val TAG = "CallListener"
    private val checkInterval: Long = 5000 // 5 seconds
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "Starting call listener for receiver ID: $receiverId")
        startListening()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "Stopping call listener")
        stopListening()
    }

    fun startListening() {
        stopListening() // Ensure we don't have multiple jobs running

        job = scope.launch {
            while (isActive) {
                try {
                    val (incoming, callId, callerId) = NetworkHelper.checkIncomingCall(receiverId)
                    if (incoming && callId != null && callerId != null) {
                        Log.d(TAG, "Incoming call detected: Call ID=$callId, Caller ID=$callerId")

                        // Get caller information
                        val (callerName, callerProfilePic) = NetworkHelper.getUserInfo(callerId)

                        val intent = Intent(context, CallReceive::class.java).apply {
                            putExtra("call_id", callId)
                            putExtra("caller_id", callerId)
                            putExtra("caller_name", callerName)
                            putExtra("caller_profile_pic", callerProfilePic)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        break // Exit the loop once a call is detected
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking for incoming calls", e)
                }
                delay(checkInterval)
            }
        }
    }

    fun stopListening() {
        job?.cancel()
        job = null
    }
}