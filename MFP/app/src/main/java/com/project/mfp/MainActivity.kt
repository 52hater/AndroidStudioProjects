package com.project.mfp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.project.mfp.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 앱이 시작될 때 화면 캡처를 예약합니다.
        scheduleScreenCapture()
    }

    // 화면 캡처를 수행하는 메서드입니다.
    fun captureScreen() {
        val rootView = window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false

        val fileName = "screenshot_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(null), fileName)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        Log.d("CaptureScreen", "Saved screenshot to ${file.absolutePath}")

        uploadImageToServer(file)
    }

    // 캡처한 이미지를 서버로 업로드하는 메서드입니다.
    private fun uploadImageToServer(file: File) {
        val url = "http://10.0.2.2:8080/upload"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Failed to upload image", e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("UploadImage", "Image uploaded successfully")
//                    file.delete()
                } else {
                    Log.e("UploadImage", "Failed to upload image: ${response.message}")
                }
            }
        })
    }

    // 화면 캡처를 주기적으로 예약하는 메서드입니다.
    private fun scheduleScreenCapture() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, CaptureReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 1분마다 화면 캡처를 수행하도록 예약합니다.
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60 * 1000,
            pendingIntent
        )
    }
}