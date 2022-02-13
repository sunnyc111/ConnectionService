package com.connectionservice

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.util.Log
import android.widget.Toast


class CallHandler(context: Context) {
    private val tag: String = CallHandler::class.toString()
    private var callManagerContext = context
    private var telecomManager: TelecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private lateinit var phoneAccountHandle: PhoneAccountHandle
    private var phoneAccount: PhoneAccount? = null

    fun init() {
        val telecomManager =
            callManagerContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val componentName =
            ComponentName(callManagerContext, CallConnectionService::class.java)
        phoneAccountHandle = PhoneAccountHandle(componentName, getConnectionServiceId())
        phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle)
        if (phoneAccount == null) { // no phone account registered yet
            val builder = PhoneAccount.builder(
                phoneAccountHandle,
                callManagerContext.resources.getText(R.string.app_name)
            )
            val uri = Uri.parse("tel:8970729294")
            builder.setSubscriptionAddress(uri)
            builder.setAddress(uri)
            builder.setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            phoneAccount = builder.build()
            telecomManager.registerPhoneAccount(phoneAccount)
        }
    }

    private fun getConnectionServiceId(): String {
        return callManagerContext.packageName + ".connectionService"
    }


    fun startOutgoingCall(numbers: MutableList<Uri>) {
        val extras = Bundle()
        extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
        val componentName = ComponentName(
            callManagerContext.packageName,
            CallConnectionService::class.java.name
        )
        val phoneAccountHandle = PhoneAccountHandle(componentName, getConnectionServiceId())
        val test = Bundle()
        test.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
        test.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras)
        try {
            if (callManagerContext.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                if(numbers.size==1) {
                    telecomManager.placeCall(Uri.parse("tel:${numbers[0]}"), test)
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        telecomManager.startConference(numbers, test)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    fun startIncomingCall() {
        Log.d(tag, "initiateCallService:")
        if (callManagerContext.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(tag, "initiateCallService: 1")
            val extras = Bundle()
            val uri =
                Uri.parse("tel:8970729294")/*Uri.fromParts(PhoneAccount.SCHEME_TEL, call.sessionId.substring(0, 11), null)*/
            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
            val isCallPermitted: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telecomManager.isIncomingCallPermitted(phoneAccountHandle)
            } else {
                true
            }
            try {
                Log.d(tag, "startIncomingCall: $extras\n$isCallPermitted")
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
            } catch (e: SecurityException) {
                Log.e("startIncomingCall:", "${e.message}")
                val intent = Intent()
                val componentName = ComponentName(
                    "com.android.server.telecom",
                    "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                )
                intent.component = componentName
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                callManagerContext.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(callManagerContext, "Error occured:" + e.message, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            Log.e("startIncomingCall: ", "Permission not granted")
        }
    }
}