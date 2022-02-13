package com.connectionservice

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*
import java.util.concurrent.TimeUnit

class HeadsUpNotificationService : Service(), OnPreparedListener {
    private val CHANNEL_ID = "ConnectionService" + "CallChannel"
    private val CHANNEL_NAME = "ConnectionService" + "Call Channel"
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mVibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private lateinit var playbackAttributes: AudioAttributes
    private var handler: Handler? = null
    private lateinit var afChangeListener: OnAudioFocusChangeListener
    private var status = false
    private var vstatus = false
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var data: Bundle? = null
        var name: String? = ""
        var callType = ""
        val NOTIFICATION_ID = 120
        try {
            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            if (audioManager != null) {
                when (audioManager.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> status = true
                    AudioManager.RINGER_MODE_SILENT -> status = false
                    AudioManager.RINGER_MODE_VIBRATE -> {
                        status = false
                        vstatus = true
                        Log.e("Service", "vibrate mode")
                    }
                }
            }
            if (status) {
                val delayedStopRunnable = Runnable { releaseMediaPlayer() }
                afChangeListener = OnAudioFocusChangeListener { focusChange ->

                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            if (mediaPlayer.isPlaying) {
                                mediaPlayer.pause()
                            }
                            handler?.postDelayed(
                                delayedStopRunnable,
                                TimeUnit.SECONDS.toMillis(30)
                            )
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                        }
                    }
                }
                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
                mediaPlayer.setLooping(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    handler = Handler()
                    playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val focusRequest =
                        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(afChangeListener, handler!!)
                            .build()
                    val res = audioManager.requestAudioFocus(focusRequest)
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        if (!keyguardManager.isDeviceLocked) {
                            mediaPlayer.start()
                        }
                    }
                } else {
                    val result = audioManager.requestAudioFocus(
                        afChangeListener,  // Use the music stream.
                        AudioManager.STREAM_MUSIC,  // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            if (!keyguardManager.isDeviceLocked) {
                                // Start playback
                                mediaPlayer.start()
                            }
                        }
                    }
                }
            } else if (vstatus) {
                mVibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                val pattern = longArrayOf(
                    0, 250, 200, 250, 150, 150, 75,
                    150, 75, 150
                )
                mVibrator.vibrate(pattern, 0)
                Log.e("Service", "vibrate mode start")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (intent?.extras != null) {
            data = intent.extras
            name = data?.getString("initiator")
            callType = "Audio"
        }
        try {
            val receiveCallAction = Intent(applicationContext, MainActivity::class.java)
            Log.d(TAG, "RECEIVE_CALL")
            receiveCallAction.putExtra(
                "ConstantApp.CALL_RESPONSE_ACTION_KEY",
                "ConstantApp.CALL_RECEIVE_ACTION"
            )
            receiveCallAction.putExtra("ACTION_TYPE", "RECEIVE_CALL")
            receiveCallAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            receiveCallAction.action = "RECEIVE_CALL"
            val cancelCallAction =
                Intent(applicationContext, CallNotificationActionReceiver::class.java)
            cancelCallAction.putExtra(
                "ConstantApp.CALL_RESPONSE_ACTION_KEY",
                "ConstantApp.CALL_CANCEL_ACTION"
            )
            cancelCallAction.putExtra("ACTION_TYPE", "CANCEL_CALL")
            cancelCallAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            cancelCallAction.action = "CANCEL_CALL"
            val callDialogAction =
                Intent(applicationContext, CallNotificationActionReceiver::class.java)
            callDialogAction.putExtra("ACTION_TYPE", "DIALOG_CALL")
            callDialogAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
            callDialogAction.action = "DIALOG_CALL"
            val receiveCallPendingIntent = PendingIntent.getActivity(
                applicationContext,
                1200,
                receiveCallAction,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val cancelCallPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                1201,
                cancelCallAction,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val callDialogPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                1202,
                callDialogAction,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            createChannel()
            var notificationBuilder: NotificationCompat.Builder? = null
            if (data != null) {
                notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(name)
                    .setContentText("Incoming $callType Call")
                    .setSmallIcon(R.drawable.ic_call_accept)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .addAction(
                        R.drawable.ic_call_decline,
                        getString(R.string.reject_call),
                        cancelCallPendingIntent
                    )
                    .addAction(
                        R.drawable.ic_call_accept,
                        getString(R.string.answer_call),
                        receiveCallPendingIntent
                    )
                    .setAutoCancel(true)
                    .setFullScreenIntent(callDialogPendingIntent, true)
            }
            var incomingCallNotification: Notification? = null
            if (notificationBuilder != null) {
                incomingCallNotification = notificationBuilder.build()
            }
            Log.d(TAG, "startForeground")
            startForeground(NOTIFICATION_ID, incomingCallNotification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        releaseVibration()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Call Notifications"
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                Objects.requireNonNull(
                    applicationContext.getSystemService(
                        NotificationManager::class.java
                    )
                ).createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun releaseVibration() {
        try {
            if (mVibrator.hasVibrator()) {
                mVibrator.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {}

    companion object {
        private val TAG = HeadsUpNotificationService::class.java.toString()
    }
}