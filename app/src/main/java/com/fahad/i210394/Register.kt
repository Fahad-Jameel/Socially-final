package com.fahad.i210394

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import org.json.JSONObject

class Register : AppCompatActivity() {

    private val url = apiconf.BASE_URL+"/Auth/signup.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<EditText>(R.id.usr)
        val emailEditText = findViewById<EditText>(R.id.email)
        val phoneNoEditText = findViewById<EditText>(R.id.phoneno)
        val passwordEditText = findViewById<EditText>(R.id.passw)
        val registerButton = findViewById<Button>(R.id.regs)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phoneNo = phoneNoEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && phoneNo.isNotEmpty() && password.isNotEmpty()) {
                signUp(username, email, phoneNo, password)
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUp(username: String, email: String, phoneNo: String, password: String) {
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    Log.d("VOLLEY_RESPONSE", "Raw response: $response")

                    val jsonObject = JSONObject(response.trim())
                    val status = jsonObject.getString("status")
                    val userId = jsonObject.optInt("user_id", -1)

                    if (status == "success") {
                        SharedPrefManager.saveUserId(this, userId)
                        val intent = Intent(this, EditProfile::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("VOLLEY", "Parse error: ${e.message}")
                    Toast.makeText(this, "Invalid server response (User Already Exists)", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("VOLLEY", "Network error: ${error.message}")
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["email"] = email
                params["phoneno"] = phoneNo
                params["password"] = password
                return params
            }
        }

        queue.add(stringRequest)
    }
}
