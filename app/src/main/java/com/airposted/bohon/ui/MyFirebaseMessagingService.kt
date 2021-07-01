package com.airposted.bohon.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val activity: Context? = null
    var activityIntent: Intent? = null

    @SuppressLint("InvalidWakeLockTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val pm: PowerManager = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (!isScreenOn) {
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MyLock"
            )
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }

        /*val dataMap: Map<String, String> = remoteMessage.data
        val title = dataMap["title"]
        val body = dataMap["text"]
        val click = dataMap["click_action"]
        val id = dataMap["id"]?.toInt()

        activityIntent = if (PersistData.getBooleanData(this, AppHelper.OPEN_SCREEN_LOAD)) {
            if (PersistentUser.getInstance().isLogged(applicationContext)) {
                Intent(applicationContext, MainActivity::class.java)
            } else {
                Intent(applicationContext, AuthActivity::class.java)
            }
        } else {
            Intent(applicationContext, IntroActivity::class.java)
        }


        activityIntent?.putExtra("notification", click)
        val activityPendingIntent =
            PendingIntent.getActivity(applicationContext, 8, activityIntent, 0)
        val pattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

        val channelId = Application.CHANNEL_1_ID
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_quick_icon)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .setVibrate(pattern)
            .setLights(Color.GREEN, 3000, 3000)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id!!, notification)*/

    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        val prefs = applicationContext.getSharedPreferences(
            "USER_PREF",
            MODE_PRIVATE
        )
        val editor = prefs.edit()
        editor.putString("FCMToken", fcmToken)
        editor.apply()
    }
}