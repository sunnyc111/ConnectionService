package com.connectionservice

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FcmMessageService : FirebaseMessagingService() {
    private val tag = FcmMessageService::class.toString()
    override fun onNewToken(token: String) {
        Log.d(tag, "Refreshed token: $token")
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(tag, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            val extras = Bundle()
            for ((key, value) in remoteMessage.data) {
                extras.putString(key, value)
            }
            if (extras.containsKey("message") && !extras.getString("message").isNullOrBlank()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val serviceIntent =
                        Intent(applicationContext, HeadsUpNotificationService::class.java)
                    val mBundle = Bundle()
                    mBundle.putString("initiator", "Santhosha Chigateri")
                    mBundle.putString("call_type", "Audio")
                    serviceIntent.putExtras(mBundle)
                    ContextCompat.startForegroundService(applicationContext, serviceIntent)
                }
            }
        }
    }
}