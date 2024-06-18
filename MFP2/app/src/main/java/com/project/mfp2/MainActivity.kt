package com.project.mfp2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.mfp2.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val storagePermissionRequestCode = 1
    private val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private val mediaProjectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, intent!!)
            startCapture()
        } else {
            Log.e("ScreenCapture", "Screen capture permission denied")
            showToast("Screen capture permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), storagePermissionRequestCode)
        } else {
            startScreenCapture()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == storagePermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScreenCapture()
            } else {
                Log.e("ScreenCapture", "Storage permission denied")
                showToast("Storage permission denied")
            }
        }
    }

    private fun startScreenCapture() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(intent)

        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun startCapture() {
        val metrics = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }
        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        scheduledExecutor.scheduleAtFixedRate({
            captureScreen()
        }, 0, 1, TimeUnit.MINUTES)
    }

    private fun captureScreen() {
        val image = imageReader?.acquireLatestImage()

        if (image != null) {
            val planes = image.planes
            val buffer: ByteBuffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            image.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String: String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            sendImageToServer(base64String)
        } else {
            Log.e("ScreenCapture", "Image is null")
        }
    }

    private fun sendImageToServer(base64String: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Base64 인코딩 옵션 수정
        val encodedString = Base64.encodeToString(base64String.toByteArray(), Base64.NO_WRAP)
        val requestBody = encodedString.toRequestBody("text/plain".toMediaTypeOrNull())

        // EC2 인스턴스의 퍼블릭 IP 주소로 변경
        val serverUrl = "http://ec2-52-78-31-144.ap-northeast-2.compute.amazonaws.com:8080/upload"

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ScreenCapture", "Failed to send image to server: ${e.message}")
                // 재시도 로직 추가
                scheduledExecutor.schedule({
                    sendImageToServer(base64String)
                }, 1, TimeUnit.MINUTES)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("ScreenCapture", "Image sent to server successfully")
                } else {
                    Log.e("ScreenCapture", "Failed to send image to server: ${response.message}")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()

        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        stopService(serviceIntent)
    }

    private fun stopCapture() {
        virtualDisplay?.release()
        mediaProjection?.stop()
        scheduledExecutor.shutdown()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
