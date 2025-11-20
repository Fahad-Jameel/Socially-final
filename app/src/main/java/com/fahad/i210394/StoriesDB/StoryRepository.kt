package com.fahad.i210394.StoriesDB
import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

object StoryRepository {
    /**
     * Fetches stories from the remote API and saves them into the local SQLite DB.
     *
     * @param context    Application or Activity context
     * @param apiUrl     Full URL of the stories‐fetch endpoint
     * @param onSuccess  Called when fetch & save succeed
     * @param onError    Called on any error (network, parse, DB)
     */
    fun syncStories(
        context: Context,
        apiUrl: String,
        onSuccess: () -> Unit,
        onError: (message: String) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            apiUrl,
            { response ->
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")
                    if (status == "success") {
                        val storiesArray = json.getJSONArray("stories")
                        val dbHelper = StoryDbHelper(context)
                        dbHelper.clearAllStories()

                        for (i in 0 until storiesArray.length()) {
                            val obj = storiesArray.getJSONObject(i)
                            val prfPic = obj.getString("profile_picture")
                            val storyPic = obj.getString("story_picture")
                            dbHelper.insertStory(prfPic, storyPic)
                        }

                        onSuccess()
                    } else {
                        val msg = json.optString("message", "Unknown error")
                        onError("Server returned failure: $msg")
                    }
                } catch (e: JSONException) {
                    onError("JSON parse error: ${e.localizedMessage}")
                }
            },
            { error ->
                onError("Network error: ${error.localizedMessage}")
            }
        )

        queue.add(request)
    }
}
