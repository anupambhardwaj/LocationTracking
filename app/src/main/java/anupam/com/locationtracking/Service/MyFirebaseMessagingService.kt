package anupam.com.locationtracking.Service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import anupam.com.locationtracking.Model.User
import anupam.com.locationtracking.R
import anupam.com.locationtracking.Utils.Common
import anupam.com.locationtracking.Utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class MyFirebaseMessagingService: FirebaseMessagingService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
            tokens.child(user!!.uid).setValue(p0)
        }

    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        if (p0!!.data != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationWithChannel(p0)
            }else {
                sendNotification(p0)
            }
            addRequestToUserInformation(p0.data)

        }
    }

    private fun sendNotification(p0: RemoteMessage) {
        val data = p0.data
        val title = "Friend Request"
        val content = "New Friend request from " + data[Common.FROM_EMAIL]!!

        val builder = NotificationCompat.Builder(this, "")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(), builder.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotificationWithChannel(p0: RemoteMessage) {

        val data = p0.data
        val title = "Friend Request"
        val content = "New Friend request from " + data[Common.FROM_EMAIL]!!

        val helper: NotificationHelper = NotificationHelper(this)
        val builder: Notification.Builder = helper.getRealtimeTrackingNotification(title, content)

        helper.getManager()!!.notify(Random().nextInt(), builder.build())


    }

    private fun addRequestToUserInformation(data: Map<String, String>) {
        //Pending Request
        val friend_request = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(data[Common.TO_UID]!!)
            .child(Common.FRIEND_REQUEST)

        val user = User(data[Common.FROM_UID]!!, data[Common.FROM_EMAIL]!!)
        friend_request.child(user.uid!!).setValue(user)

    }
}