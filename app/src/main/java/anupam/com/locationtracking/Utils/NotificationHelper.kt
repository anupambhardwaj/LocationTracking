package anupam.com.locationtracking.Utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import anupam.com.locationtracking.R

class NotificationHelper(base: Context): ContextWrapper(base) {

    companion object{
        private val MY_CHANNEL_ID = "anupam.com.locationtracking"
        private val MY_CHANNEL_NAME = "KotlinRealtimeApp"
    }

    private var manager: NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(uri: Uri?) {
        val myChannel = NotificationChannel(MY_CHANNEL_ID, MY_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        myChannel.enableLights(true)
        myChannel.enableVibration(true)
        myChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()

        myChannel.setSound(uri!!, audioAttributes)

        getManager()!!.createNotificationChannel(myChannel)


    }

    fun getManager(): NotificationManager {
        if (manager == null){
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager!!

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRealtimeTrackingNotification(title: String, content: String): Notification.Builder{
        return Notification.Builder(applicationContext, MY_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)
    }

}