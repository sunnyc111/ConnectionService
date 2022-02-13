package com.connectionservice

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.connectionservice.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    private val tag: String = MainActivity::class.toString()
    private lateinit var viewBinding: ActivityMainBinding
    private var callHandler: CallHandler? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        callHandler = CallHandler(applicationContext)
        callHandler!!.init()

        requestPermission()
        fetchFCMToken()
        val action: String? = intent.getStringExtra("ACTION_TYPE")
        action?.let { initiateCallService(it) }
    }

    private fun requestPermission() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            val startForResult =
                registerForActivityResult(ActivityResultContracts.RequestPermission())
                { isGranted: Boolean ->
                    if (isGranted) {
                        showPhoneAccount()
                    }
                }
            startForResult.launch(
                Manifest.permission.CALL_PHONE
            )
        }
    }

    private fun fetchFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(tag, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d(tag, "FCM token: $token")
        })
    }

    private fun initiateCallService(action: String) {
        Log.d(tag, "initiateCallService:")
        stopService(Intent(applicationContext, HeadsUpNotificationService::class.java))
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        sendBroadcast(it)
        try {
            if (action == "RECEIVE_CALL")
                callHandler?.startIncomingCall()

        } catch (e: Exception) {
            Log.e("initiateCallError:", "${e.message}")
            Toast.makeText(
                applicationContext,
                "Unable to receive call due to " + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPhoneAccount() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.deflect_call)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("OK") { dialogInterface, which ->
            val intent = Intent()
            intent.component = ComponentName(
                "com.android.server.telecom",
                "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    fun dial(view: View) {
        viewBinding.number.text?.let {
            if(!it.toString().equals("")) {
                val number = it.toString()
                val uri = Uri.parse("tel:$number")
                var numbers = mutableListOf<Uri>(uri)
                callHandler?.startOutgoingCall(numbers)
            }
        }
    }

    fun dialConferenceCall(view: View) {
        val uri1 = Uri.parse("tel:9876474898")
        val uri2 = Uri.parse("tel:9876474345")
        val uri3 = Uri.parse("tel:9876474756")

        var numbers = mutableListOf<Uri>(uri1, uri2, uri3)
        callHandler?.startOutgoingCall(numbers)

    }
}