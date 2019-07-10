package anupam.com.locationtracking

import android.graphics.Typeface
import android.os.Bundle
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
import anupam.com.locationtracking.Model.User
import anupam.com.locationtracking.Utils.Common
import anupam.com.locationtracking.ViewHolder.FriendRequestViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_all_people.*
import kotlinx.android.synthetic.main.activity_all_people.material_search_bar
import kotlinx.android.synthetic.main.activity_friend_request.*

class FriendRequestActivity : AppCompatActivity(), IFirebaseLoadDone {

    var adapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>? = null
    var searchAdapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>? = null

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        material_search_bar.setCardViewElevation(10)
        material_search_bar.addTextChangeListener(object: TextWatcher {
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
                        recycler_friend_request.adapter = adapter
                    }
                }
            }


            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

        })
        recycler_friend_request.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_friend_request.layoutManager = layoutManager
        recycler_friend_request.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        iFirebaseLoadDone = this

        loadFriendRequestList()
        loadSearchData()

    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

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

    private fun startSearch(search: String) {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)
            .orderByChild("email")
            .startAt(search)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_friend_request, viewGroup, false)
                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.btn_accept.setOnClickListener{
                    deleteFriendRequest(model, false)
                    AddToAcceptList(model) //Add your friends to your friendslist
                    AddUserToFriendContact(model) //Add you to friendslist of your friend
                }

                holder.btn_decline.setOnClickListener {
                    deleteFriendRequest(model, true)
                }
            }

        }
        searchAdapter!!.startListening()
        recycler_friend_request.adapter = searchAdapter


    }

    private fun loadFriendRequestList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_friend_request, viewGroup, false)
                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.btn_accept.setOnClickListener{
                    deleteFriendRequest(model, false)
                    AddToAcceptList(model) //Add your friends to your friendslist
                    AddUserToFriendContact(model) //Add you to friendslist of your friend
                }

                holder.btn_decline.setOnClickListener { 
                    deleteFriendRequest(model, true)
                }
            }

        }
        adapter!!.startListening()
        recycler_friend_request.adapter = adapter

    }

    private fun AddUserToFriendContact(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(model.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(Common.loggedUser!!.uid!!).setValue(Common.loggedUser)

    }

    private fun AddToAcceptList(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(model.uid!!).setValue(model)

    }

    private fun deleteFriendRequest(model: User, isShowMessage: Boolean){
        val friendRequest = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        friendRequest.child(model.uid!!).removeValue().addOnSuccessListener {
            if (isShowMessage){
                Toast.makeText(this, "Remove!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStop() {
        if(adapter != null){
            adapter!!.stopListening()
        }
        if(searchAdapter != null){
            searchAdapter!!.stopListening()
        }
        super.onStop()
    }

    override fun onFireBaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFireBaseLoadFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
