package com.project.mfp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CaptureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MainActivity의 인스턴스를 가져옵니다.
        val mainActivity = MainActivity()

        // MainActivity의 captureScreen() 메서드를 호출합니다.
        mainActivity.captureScreen()
    }
}