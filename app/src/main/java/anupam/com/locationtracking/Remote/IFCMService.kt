package anupam.com.locationtracking.Remote

import anupam.com.locationtracking.Model.MyResponse
import anupam.com.locationtracking.Model.Request
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers("Content-Type:application/json", "Authorization:key=AAAAtZTX2Bo:APA91bH-TDgwf64IAsb5V_Qm_rc0x_inbqX9smekus8I1JzE01FMSUncO4LmnOLUTd3nocntcfNctU6A2ifGYVGNIghOhgMR8rkhhtueWqa9XZjyLJkURGWp_pIcwAoU_dGtwOjVLHax")
    @POST("fcm/send")
    fun sendFriendRequestToUser(@Body body: Request):Observable<MyResponse>

}
