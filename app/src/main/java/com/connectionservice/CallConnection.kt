package com.connectionservice

import android.content.Context
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log

class CallConnection(context: Context) : Connection() {

    private val tag = CallConnection::class.toString()

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        Log.e(tag, "onCallAudioStateChange:" + state.toString())
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
        Log.e(tag, "onStateChanged: $state")
    }

    override fun onDisconnect() {
        super.onDisconnect()
        destroyConnection()
        Log.e(tag, "onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL, "Missed"))

    }

    override fun onHold() {
        super.onHold()
    }


    override fun onCallEvent(event: String?, extras: Bundle?) {
        super.onCallEvent(event, extras)
        Log.e(tag, "onCallEvent: $event")
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }

    override fun onAnswer() {
        Log.e(tag, "onAnswer: ")
    }

    private fun destroyConnection() {
        setDisconnected(DisconnectCause(DisconnectCause.REMOTE, "Rejected"))
        Log.e(tag, "destroyConnection")
        super.destroy()
    }

    override fun onReject() {
        Log.e(tag, "onReject: ")
    }

    override fun onAbort() {
        super.onAbort()
        Log.e(tag, "OnAbort")
    }

    fun onOutgoingReject() {
        Log.e(tag, "onDisconnect")
        destroyConnection()
        setDisconnected(DisconnectCause(DisconnectCause.REMOTE, "REJECTED"))
    }

    override fun onReject(rejectReason: Int) {
        Log.e(tag, "onReject: $rejectReason")
        super.onReject(rejectReason)
    }

    override fun onReject(replyMessage: String?) {
        Log.e(tag, "onReject: $replyMessage")
        super.onReject(replyMessage)
    }
}