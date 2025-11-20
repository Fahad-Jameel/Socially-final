package com.fahad.i210394

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostCamera : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var capturedImage: ImageView
    private lateinit var galleryButton: ImageButton
    private lateinit var captureButton: ImageButton
    private lateinit var switchCameraButton: ImageButton

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraExecutor: ExecutorService

    private val REQUEST_CAMERA_PERMISSION = 1001
    private val REQUEST_GALLERY_PERMISSION = 1002
    private val PICK_IMAGE_REQUEST = 1003

    companion object {
        // Use a companion object to store the temporary image
        var tempImageUri: Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_camera)

        setupToolbar()
        initializeViews()
        requestPermissions()

        cameraExecutor = Executors.newSingleThreadExecutor()
        Log.d("PostCamera", "Activity Created")
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializeViews() {
        previewView = findViewById(R.id.previewView)
        capturedImage = findViewById(R.id.capturedImage)
        captureButton = findViewById(R.id.captureButton)
        galleryButton = findViewById(R.id.squareView)
        switchCameraButton = findViewById(R.id.circleView)

        captureButton.setOnClickListener { takePhoto() }
        galleryButton.setOnClickListener { openGallery() }
        switchCameraButton.setOnClickListener { toggleCamera() }

        Log.d("PostCamera", "Views Initialized")
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        // For Android 13+ (API level 33+), we need READ_MEDIA_IMAGES instead of READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty()) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions required to use the camera and access gallery", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchGalleryIntent()
        } else {
            Log.e("CameraX", "Permission denied")
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner, cameraSelector, preview, imageCapture
                )
                Log.d("PostCamera", "Camera Started")
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create temporary file for storing the captured image
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "img_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    savedUri?.let {
                        try {
                            // Store the URI in the companion object
                            tempImageUri = it
                            // Navigate to PostComplete (without passing the image data)
                            navigateToPostComplete()
                            Log.d("PostCamera", "Photo Captured: $it")
                        } catch (e: Exception) {
                            Log.e("PostCamera", "Error processing captured image: ${e.message}", e)
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun openGallery() {
        // Check for the appropriate permission based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_GALLERY_PERMISSION
                )
            } else {
                launchGalleryIntent()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_GALLERY_PERMISSION
                )
            } else {
                launchGalleryIntent()
            }
        }
    }

    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                try {
                    // Store the URI in the companion object
                    tempImageUri = selectedImageUri
                    // Navigate to PostComplete
                    navigateToPostComplete()
                    Log.d("PostCamera", "Gallery Image Selected: $selectedImageUri")
                } catch (e: Exception) {
                    Log.e("PostCamera", "Error processing gallery image: ${e.message}", e)
                    Toast.makeText(this, "Error processing selected image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun navigateToPostComplete() {
        val intent = Intent(this, PostComplete::class.java)
        tempImageUri?.let {
            intent.putExtra("image_uri", it.toString())
        }
        startActivity(intent)
        Log.d("PostCamera", "Navigating to PostComplete")
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}