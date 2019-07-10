package anupam.com.locationtracking

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import anupam.com.locationtracking.Interface.IFirebaseLoadDone
import anupam.com.locationtracking.Interface.IRecyclerItemClickListener
import anupam.com.locationtracking.Model.MyResponse
import anupam.com.locationtracking.Model.Request
import anupam.com.locationtracking.Model.User
import anupam.com.locationtracking.Utils.Common
import anupam.com.locationtracking.ViewHolder.UserViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_all_people.*

class AllPeopleActivity : AppCompatActivity(), IFirebaseLoadDone {

    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList: List<String> = ArrayList()

    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_people)


        material_search_bar.setCardViewElevation(10)
        material_search_bar.addTextChangeListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val suggest = ArrayList<String>()
                for (search in suggestList){
                    if (search.toLowerCase().contentEquals(material_search_bar.text.toLowerCase())){
                        suggest.add(search)
                    }
                }
                material_search_bar.lastSuggestions = suggest
            }

        })
        material_search_bar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onButtonClicked(buttonCode: Int) {

            }

            override fun onSearchStateChanged(enabled: Boolean) {
                if (!enabled){
                    //Close search -> return default
                    if (adapter != null){
                        recycler_all_people.adapter = adapter
                    }
                }
            }


            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

        })

        recycler_all_people.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_all_people.layoutManager = layoutManager
        recycler_all_people.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        iFirebaseLoadDone = this

        loadUserList()
        loadSearchData()

    }

    private fun startSearch(search: String) {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO).orderByChild("email").startAt(search)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object: FirebaseRecyclerAdapter<User, UserViewHolder>(options){
            override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): UserViewHolder {
                val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_user, viewGroup, false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if (model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = StringBuilder(model.email!!).append(" (me) ")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface, Typeface.ITALIC)
                }else {
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                //Event
                holder.setClick(object : IRecyclerItemClickListener {
                    override fun onItemClickListenere(view: View, position: Int) {
                        showDialogRequest(model)
                    }
                })
            }

        }
        searchAdapter!!.startListening()
        recycler_all_people.adapter = searchAdapter

    }

    private fun showDialogRequest(model: User) {
        val alertDialog = AlertDialog.Builder(this, R.style.MyRequestDialog)
        alertDialog.setTitle("Request Friend")
        alertDialog.setMessage("Do you want to send request friend to" + model.email)
        alertDialog.setIcon(R.drawable.ic_person_add_black_24dp)

        alertDialog.setNegativeButton("Cancel", {dialogInterface, _ -> dialogInterface.dismiss()})

        alertDialog.setPositiveButton("Send") { _, _ ->
            val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
                .child(Common.loggedUser!!.uid!!)
                .child(Common.ACCEPT_LIST)

            //Check from user friend list to make sure is not friend before
            acceptList.orderByKey().equalTo(model.uid).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null){//Not friend
                        sendFriendRequest(model)
                    }else {
                        Toast.makeText(this@AllPeopleActivity, "You and " + model.email + " are friends already!)", Toast.LENGTH_SHORT).show()
                    }
                }

            } )
        }

        alertDialog.show()
    }

    private fun sendFriendRequest(model: User) {
        //Get Token to send friend request
        val token = FirebaseDatabase.getInstance().getReference(Common.TOKENS)

        token.orderByKey().equalTo(model.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null){//Not available token
                    Toast.makeText(this@AllPeopleActivity, "Token error", Toast.LENGTH_SHORT).show()
                }else {
                    //Create request
                    val request = Request()
                    val dataSend = HashMap<String, String>()
                    dataSend[Common.FROM_UID] = Common.loggedUser!!.uid!! // my UID
                    dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email!! // my email
                    dataSend[Common.TO_UID] = model.uid!! // my friend UID
                    dataSend[Common.TO_EMAIL] = model.email!! // my friend email

                    //Set request
                    request.to = dataSnapshot.child(model.uid!!).getValue(String::class.java)!!
                    request.data = dataSend

                    //Send
                    compositeDisposable.add(Common.fcmService.sendFriendRequestToUser(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({t: MyResponse? ->
                            if (t!!.success == 1){
                                Toast.makeText(this@AllPeopleActivity, "Request send", Toast.LENGTH_SHORT).show()
                            }
                        },{t: Throwable? ->
                            Toast.makeText(this@AllPeopleActivity, t!!.message, Toast.LENGTH_SHORT).show()
                        }))

                }
            }
        })
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)

        userList.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                iFirebaseLoadDone.onFireBaseLoadFailed(p0.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapShot in dataSnapshot.children){
                    val user = userSnapShot.getValue(User::class.java)
                    lstUserEmail.add(user!!.email!!)
                }
                iFirebaseLoadDone.onFireBaseLoadUserDone(lstUserEmail)
            }

        })
    }

    private fun loadUserList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object: FirebaseRecyclerAdapter<User, UserViewHolder>(options){
            override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): UserViewHolder {
                val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_user, viewGroup, false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if (model.email.equals(Common.loggedUser!!.email)){
                    holder.txt_user_email.text = StringBuilder(model.email!!).append(" (me) ")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface, Typeface.ITALIC)
                }else {
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                //Event
                holder.setClick(object : IRecyclerItemClickListener {
                    override fun onItemClickListenere(view: View, position: Int) {
                        showDialogRequest(model)
                    }
                })
            }

        }
        adapter!!.startListening()
        recycler_all_people.adapter = adapter
    }

    override fun onStop() {
        if(adapter != null){
            adapter!!.stopListening()
        }
        if(searchAdapter != null){
            searchAdapter!!.stopListening()
        }
        compositeDisposable.clear()
        super.onStop()

    }

    override fun onFireBaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFireBaseLoadFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
