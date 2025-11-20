package com.fahad.i210394

import android.Manifest
import android.app.ProgressDialog
import android.app.VoiceInteractor
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fahad.i210394.apiconfig.apiconf
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Story : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var capturedImageView: ImageView
    private lateinit var captureButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var flipCameraButton: ImageButton
    private lateinit var nextButton: TextView
    private lateinit var toolbar: Toolbar

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var capturedImageUri: Uri? = null
    private var isImageCaptured = false
    // New flag to avoid repeated permission checks
    private var permissionsChecked = false

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    // Single permission launcher for both camera and storage
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Mark permissions as checked to avoid repeated prompts
        permissionsChecked = true

        var cameraGranted = true
        var storageGranted = true

        // Check which permissions were granted
        permissions.entries.forEach {
            when (it.key) {
                Manifest.permission.CAMERA -> {
                    cameraGranted = it.value
                }
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    if (!it.value) storageGranted = false
                }
            }
        }

        // Start camera if permission was granted
        if (cameraGranted) {
            startCamera()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            previewView.visibility = View.GONE
            capturedImageView.visibility = View.VISIBLE
            capturedImageView.setImageURI(uri)
            capturedImageUri = uri
            isImageCaptured = true
            nextButton.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_story)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        previewView = findViewById(R.id.previewView)
        capturedImageView = findViewById(R.id.capturedImage)
        captureButton = findViewById(R.id.captureButton)
        galleryButton = findViewById(R.id.squareView)
        flipCameraButton = findViewById(R.id.circleView)
        nextButton = findViewById(R.id.callpers)
        toolbar = findViewById(R.id.toolbar)

        // Set up toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Initially hide the Next button until image is captured
        nextButton.visibility = View.GONE

        // Set up click listeners
        captureButton.setOnClickListener {
            takePhoto()
        }

        galleryButton.setOnClickListener {
            pickImageFromGallery()
        }

        flipCameraButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }

        nextButton.setOnClickListener {
            if (isImageCaptured && capturedImageUri != null) {
                uploadStory()
            } else {
                Toast.makeText(this, "Please capture or select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request permissions only once at startup
        checkPermissionsAndStartCamera()
    }

    // Simplified permission check that only runs once
    private fun checkPermissionsAndStartCamera() {
        // If we've already checked permissions, don't check again
        if (permissionsChecked) {
            if (hasCameraPermission()) {
                startCamera()
            }
            return
        }

        // Check if we already have permissions
        if (allPermissionsGranted()) {
            permissionsChecked = true
            startCamera()
            return
        }

        // Request permissions if not granted
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Helper method to check if all permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Helper method to check only camera permission
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Helper method to check storage permissions
    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Assume permissions are granted if we got this far
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(baseContext, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    if (savedUri != null) {
                        previewView.visibility = View.GONE
                        capturedImageView.visibility = View.VISIBLE
                        capturedImageView.setImageURI(savedUri)
                        capturedImageUri = savedUri
                        isImageCaptured = true
                        nextButton.visibility = View.VISIBLE
                    }
                }
            }
        )
    }

    private fun pickImageFromGallery() {
        // Assume permissions are granted if we got this far
        pickImageLauncher.launch("image/*")
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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    private fun uploadStory() {
        val userId = SharedPrefManager.getUserId(this)
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Load bitmap from the URI
        val uri = capturedImageUri!!
        val bitmap = contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input)
        } ?: run {
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. (Optional) Resize to avoid huge payloads
        val resized = resizeBitmap(bitmap, 800)

        // 4. Convert to Base64
        val imageBase64 = bitmapToBase64(resized)


        val url = "${apiconf.BASE_URL}Story/createstory.php"
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(Method.POST, url,
            { response ->
                // parse JSON response
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        Toast.makeText(this,
                            "Story #${json.getInt("story_id")} created!",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this,
                            "Error: ${json.getString("message")}",
                            Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this,
                    "Upload failed: ${error.localizedMessage}",
                    Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    // these keys must match your PHP:
                    "id"      to userId.toString(),
                    "picture" to imageBase64
                )
            }
        }

        val progress = ProgressDialog(this).apply {
            setMessage("Uploading...")
            setCancelable(false)
            show()
        }
        request.setRetryPolicy(
            DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        queue.add(request).also {
            it.addMarker("uploadStory")
            it.setShouldCache(false)
        }
        // Dismiss dialog on complete
        queue.addRequestFinishedListener<VoiceInteractor.Request> { progress.dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}