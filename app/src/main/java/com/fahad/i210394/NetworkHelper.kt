package com.fahad.i210394

import android.util.Log
import com.fahad.i210394.apiconfig.apiconf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object NetworkHelper {
    private const val TAG = "NetworkHelper"
    private const val SERVER_URL = apiconf.BASE_URL + "/Calls/"

    suspend fun startCall(callerId: Int, receiverId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(SERVER_URL + "start_call.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val params = "caller_id=$callerId&receiver_id=$receiverId"
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(params)
                writer.flush()

                val responseCode = conn.responseCode
                Log.d(TAG, "Start call response code: $responseCode")
                responseCode == 200
            } catch (e: Exception) {
                Log.e(TAG, "Error starting call", e)
                false
            }
        }
    }

    suspend fun checkIncomingCall(receiverId: Int): Triple<Boolean, Int?, Int?> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(SERVER_URL + "check_call_status.php?receiver_id=$receiverId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                val inputStream = BufferedReader(InputStreamReader(conn.inputStream))
                val response = inputStream.readText()
                Log.d(TAG, "Check call response: $response")

                val jsonObject = JSONObject(response)
                if (jsonObject.optBoolean("incoming", false)) {
                    val callId = jsonObject.optInt("call_id")
                    val callerId = jsonObject.optInt("caller_id")
                    Triple(true, callId, callerId)
                } else {
                    Triple(false, null, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking incoming call", e)
                Triple(false, null, null)
            }
        }
    }

    suspend fun updateCallStatus(callId: Int, status: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(SERVER_URL + "update_call_status.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val params = "call_id=$callId&status=$status"
                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(params)
                writer.flush()

                val responseCode = conn.responseCode
                Log.d(TAG, "Update call status response code: $responseCode")
                responseCode == 200
            } catch (e: Exception) {
                Log.e(TAG, "Error updating call status", e)
                false
            }
        }
    }

    // New method to get user information (name, profile picture, etc.)
    suspend fun getUserInfo(userId: Int): Pair<String, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(SERVER_URL + "get_user_info.php?user_id=$userId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connect()

                val inputStream = BufferedReader(InputStreamReader(conn.inputStream))
                val response = inputStream.readText()
                Log.d(TAG, "Get user info response: $response")

                val jsonObject = JSONObject(response)
                val name = jsonObject.optString("name", "Unknown User")
                val profilePicture = jsonObject.optString("profile_picture", null)

                Pair(name, profilePicture)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user info", e)
                Pair("Unknown User", null)
            }
        }
    }
}