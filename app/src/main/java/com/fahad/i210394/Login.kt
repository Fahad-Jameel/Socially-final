package com.fahad.i210394

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import org.json.JSONObject

class Login : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val regText = findViewById<TextView>(R.id.regt)
        val login = findViewById<Button>(R.id.log)
        val email = findViewById<EditText>(R.id.usr)
        val password = findViewById<EditText>(R.id.passw)

        login.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()

            if (emailText.isNotEmpty() && passText.isNotEmpty()) {
                performLogin(emailText, passText)
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        regText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(email: String, password: String) {
        val url = apiconf.BASE_URL + "/Auth/login.php"

        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getBoolean("success")) {
                        val id = jsonResponse.getInt("user_id") // Corrected key

                        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("user_id", id)
                            apply()
                        }

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Navigation::class.java))
                        finish()
                    } else {
                        val message = jsonResponse.optString("message", "Login failed")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Login failed: Invalid response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "email" to email,
                    "password" to password
                )
            }
        }

        queue.add(request)
    }


}
