package com.fahad.i210394

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EditProfile : AppCompatActivity() {
    private lateinit var imageView: CircleImageView
    private var base64Image: String? = null
    private var userID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        userID = SharedPrefManager.getUserId(this)
        imageView = findViewById(R.id.prfpic)
        val saveBtn = findViewById<TextView>(R.id.save)

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        saveBtn.setOnClickListener {
            sendProfileToServer()
        }
    }

    override fun onStart() {
        super.onStart()
        val url = apiconf.BASE_URL + "profile/getProfile.php"
        val name = findViewById<TextView>(R.id.nametit)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.getString("status") == "success"
                    if (success) {
                        val profile = jsonObject.getJSONObject("profile")
                        val usernameText = profile.getString("username")
                        val contactText = profile.getString("contact")
                        val bioText = profile.getString("bio")
                        val pictureBase64 = profile.getString("picture")

                        val usernameField = findViewById<EditText>(R.id.usrname)
                        val contactField = findViewById<EditText>(R.id.contact)
                        val bioField = findViewById<EditText>(R.id.bio)

                        usernameField.setText(usernameText)
                        contactField.setText(contactText)
                        bioField.setText(bioText)
                        name.setText(usernameText)

                        // Decode and display the picture
                        val imageBytes = Base64.decode(pictureBase64, Base64.DEFAULT)
                        val inputStream = ByteArrayInputStream(imageBytes)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageView.setImageBitmap(bitmap)

                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show()
                    Log.e("E", "error is: $e")
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userID.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageView.setImageURI(imageUri)

            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val bitmap2 = resizeBitmap(bitmap,500)
            base64Image = encodeImageToBase64(bitmap2)
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
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

    private fun sendProfileToServer() {
        // Get data from EditText fields
        val username = findViewById<EditText>(R.id.usrname).text.toString()
        val contact = findViewById<EditText>(R.id.contact).text.toString()
        val bio = findViewById<EditText>(R.id.bio).text.toString()
        val picture = base64Image ?: ""


        val url = apiconf.BASE_URL + "profile/editprofile.php"  // or your actual file name

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Log.e("E", "sendProfileToServer: $response")
                Toast.makeText(this, "Server: $response", Toast.LENGTH_LONG).show()
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = userID.toString()  // Use the user ID from shared preferences
                params["username"] = username
                params["contact"] = contact
                params["bio"] = bio
                params["picture"] = picture
                return params
            }
        }

        requestQueue.add(stringRequest)
    }


}