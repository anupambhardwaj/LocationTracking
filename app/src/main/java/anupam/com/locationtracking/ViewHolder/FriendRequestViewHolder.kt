package anupam.com.locationtracking.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import anupam.com.locationtracking.R

class FriendRequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    var txt_user_email: TextView
    var btn_accept: ImageView
    var btn_decline: ImageView

    init {
        txt_user_email = itemView.findViewById(R.id.txt_user_email) as TextView
        btn_accept = itemView.findViewById(R.id.btn_accept) as ImageView
        btn_decline = itemView.findViewById(R.id.btn_decline) as ImageView


    }
}