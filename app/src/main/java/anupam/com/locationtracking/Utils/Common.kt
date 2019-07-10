package anupam.com.locationtracking.Utils

import anupam.com.locationtracking.Model.User
import anupam.com.locationtracking.Remote.IFCMService
import anupam.com.locationtracking.Remote.RetrofitClient
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object Common {
    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)

    }

    fun getFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()

    }

    var trackingUser: User? = null
    val PUBLIC_LOCATION: String = "PublicLocation"
    val FRIEND_REQUEST: String = "FriendRequest"
    val TO_EMAIL: String = "ToEmail"
    val TO_UID: String = "ToUid"
    val FROM_EMAIL: String = "FromEmail"
    val FROM_UID: String = "FromUid"
    val ACCEPT_LIST: String = "acceptList"
    val USER_UID_SAVE_KEY: String = "SAVE_KEY"
    val TOKENS: String = "Tokens"
    var loggedUser: User? = null
    val USER_INFO: String = "UserInfo"

    val fcmService: IFCMService
    get() = RetrofitClient.getClient("https://fcm.googleapis.com/").create(IFCMService::class.java)
}