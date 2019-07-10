package anupam.com.locationtracking.ViewHolder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import anupam.com.locationtracking.Interface.IRecyclerItemClickListener
import anupam.com.locationtracking.R

class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var txt_user_email: TextView
    lateinit var iRecyclerItemClickListener: IRecyclerItemClickListener

    fun setClick(iRecyclerItemClickListener: IRecyclerItemClickListener){
        this.iRecyclerItemClickListener = iRecyclerItemClickListener
    }

    init {
        txt_user_email = itemView.findViewById(R.id.txt_user_email) as TextView

        itemView.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        iRecyclerItemClickListener.onItemClickListenere(v!!, adapterPosition)
    }
}