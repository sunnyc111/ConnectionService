package com.connectionservice

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log

class CallNotificationActionReceiver : BroadcastReceiver() {
    private var mContext: Context? = null
    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        if (intent.extras != null) {
            Log.d(TAG, "onReceive")
            val action: String? = intent.getStringExtra("ACTION_TYPE")
            Log.d(TAG, "onReceive = $action")
            if (action != null && !action.equals("", ignoreCase = true)) {
                performClickAction(context, action)
            }
            // Close the notification after the click action is performed.
            val close = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(close)
            context.stopService(Intent(context, HeadsUpNotificationService::class.java))
        }
    }

    private fun performClickAction(context: Context, action: String) {
        if (action.equals("RECEIVE_CALL", ignoreCase = true)) {
            if (checkAppPermissions()) {
                Log.d(TAG, "RECEIVE_CALL")
                val intentCallReceive =
                    Intent(mContext!!.applicationContext, MainActivity::class.java)
                intentCallReceive.putExtra("Call", "incoming")
                intentCallReceive.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                mContext!!.startActivity(intentCallReceive)
            }
        } else if (action.equals("DIALOG_CALL", ignoreCase = true)) {

            // show ringing activity when phone is locked
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            mContext!!.startActivity(intent)
        } else {
            Log.d(TAG, "REJECT_CALL")
            context.stopService(Intent(context, HeadsUpNotificationService::class.java))
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
        }
    }

    private fun checkAppPermissions(): Boolean {
        return hasReadPermissions() && hasWritePermissions() && hasCameraPermissions() && hasAudioPermissions()
    }

    private fun hasAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            mContext!!,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val TAG = CallNotificationActionReceiver::class.java.toString()
    }
}