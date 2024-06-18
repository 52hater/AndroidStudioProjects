package com.project.mfp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.mfp.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val PERMISSION_REQUEST_CODE = 1
    private val SCREENSHOT_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // MediaProjectionManager 초기화
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // 화면 캡처 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Android 11 이상 에서는 MANAGE_EXTERNAL_STORAGE 권한 요청
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // Android 11 미만 에서는 WRITE_EXTERNAL_STORAGE 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            startScreenCapture()
        }
    }

    // 화면 캡처 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScreenCapture()
            } else {
                Log.e("ScreenCapture", "Storage permission denied")
                finish()
            }
        }
    }

    // 화면 캡처 시작
    private fun startScreenCapture() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, SCREENSHOT_REQUEST_CODE)
    }

    // 화면 캡처 권한 요청 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCREENSHOT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)
                startCapture()
            } else {
                Log.e("ScreenCapture", "Screen capture permission denied")
                finish()
            }
        }
    }

    // 화면 캡처 시작
    private fun startCapture() {
        val metrics = resources.displayMetrics
        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, ImageFormat.FLEX_RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        // 1분마다 화면 캡처 반복
        val handler = Handler(Looper.getMainLooper())
        val delay = TimeUnit.MINUTES.toMillis(1)

        handler.postDelayed(object : Runnable {
            override fun run() {
                captureScreen()
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    // 화면 캡처
    private fun captureScreen() {
        val image = imageReader?.acquireLatestImage()

        if (image != null) {
            val planes = image.planes
            val buffer = planes[0].buffer
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

            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            sendImageToServer(base64String)
        }
    }

    // 서버로 이미지 전송
    private fun sendImageToServer(base64String: String) {
        val client = OkHttpClient()
        val requestBody = base64String.toRequestBody("text/plain".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ScreenCapture", "Failed to send image to server: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ScreenCapture", "Image sent to server successfully")
            }
        })
    }

    // 앱 종료 시 캡처 중지
    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
    }

    // 캡처 중지
    private fun stopCapture() {
        virtualDisplay?.release()
        mediaProjection?.stop()
    }
}