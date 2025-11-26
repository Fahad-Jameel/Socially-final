package com.fahad.i210394

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class PostComplete : AppCompatActivity() {


    private lateinit var imageView: ImageView
    private lateinit var captionInput: EditText
    private lateinit var shareButton: Button
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_complete)

        imageView = findViewById(R.id.finalPostImage)
        captionInput = findViewById(R.id.captionEditText)
        shareButton = findViewById(R.id.shareButton)

        val buttonC = findViewById<ImageButton>(R.id.closebutt)
        buttonC.setOnClickListener {
            val intent = Intent(this, Navigation::class.java)
            startActivity(intent)
        }

        // Show the selected image
        PostCamera.tempImageUri?.let {
            imageUri = it
            imageView.setImageURI(it)
        }

        // Handle share button
        shareButton.setOnClickListener {
            if (imageUri != null && captionInput.text.isNotBlank()) {
                uploadPost(imageUri!!, captionInput.text.toString())
            } else {
                Toast.makeText(this, "Caption or image is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPost(imageUri: Uri, caption: String) {
        val context = this@PostComplete
        val queue = Volley.newRequestQueue(context)

        thread {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val compressedBitmap = resizeBitmap(bitmap, 800) // Compress image to 800px max dimension (adjust as needed)
                val base64Image = bitmapToBase64(compressedBitmap)

                val userId = SharedPrefManager.getUserId(this)
                val apiUrl = "${apiconf.BASE_URL}Post/createpost.php"

                val postData = "id=$userId&picture=$base64Image&caption=$caption"

                // Create a POST request using Volley
                val request = object : StringRequest(
                    Request.Method.POST, apiUrl,
                    Response.Listener { response ->
                        try {
                            val jsonResponse = JSONObject(response)
                            if (jsonResponse.getString("status") == "success") {
                                runOnUiThread {
                                    Toast.makeText(this@PostComplete, "Post uploaded successfully", Toast.LENGTH_SHORT).show()

                                    // Redirect to Navigation or your preferred activity
                                    val intent = Intent(this@PostComplete, Navigation::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@PostComplete, "Failed: ${jsonResponse.getString("message")}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@PostComplete, "Error processing response: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    Response.ErrorListener { error ->
                        runOnUiThread {
                            Toast.makeText(this@PostComplete, "Network Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }) {

                    override fun getParams(): MutableMap<String, String> {
                        val params = mutableMapOf<String, String>()
                        params["id"] = userId.toString()
                        params["picture"] = base64Image
                        params["caption"] = caption
                        return params
                    }
                }

                // Add the request to the queue
                queue.add(request)

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@PostComplete, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)  // Compress at 100% quality
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)  // Use Base64.DEFAULT encoding
    }

    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (newWidth / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (newHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}