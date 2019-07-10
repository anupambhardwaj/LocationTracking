package anupam.com.locationtracking

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import anupam.com.locationtracking.Interface.IFirebaseLoadDone
import anupam.com.locationtracking.Interface.IRecyclerItemClickListener
import anupam.com.locationtracking.Model.User
import anupam.com.locationtracking.Service.MyLocationReceiver
import anupam.com.locationtracking.Utils.Common
import anupam.com.locationtracking.ViewHolder.UserViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {

    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null

    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList: List<String> = ArrayList()

    lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            startActivity(Intent(this, AllPeopleActivity::class.java))
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val headerView = nav_view.getHeaderView(0)
        val user_email = headerView.findViewById<View>(R.id.user_email) as TextView
        user_email.text = Common.loggedUser!!.email!!

        //View
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
                        recycler_friend_list.adapter = adapter
                    }
                }
            }


            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

        })

        recycler_friend_list.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_friend_list.layoutManager = layoutManager
        recycler_friend_list.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        loadFriendList()
        loadSearchData()

        iFirebaseLoadDone = this

        updateLocation()
    }

    private fun updateLocation() {
        buildLocationRequest()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this@HomeActivity, MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.smallestDisplacement = 10f
        locationRequest.fastestInterval = 3000
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

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
            .child(Common.ACCEPT_LIST)
            .orderByChild("email")
            .startAt(search)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object: FirebaseRecyclerAdapter<User, UserViewHolder>(options){
            override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): UserViewHolder {
                val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_user, viewGroup, false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object : IRecyclerItemClickListener {
                    override fun onItemClickListenere(view: View, position: Int) {
                        Common.trackingUser = model
                        startActivity(Intent(this@HomeActivity, TrackingActivity::class.java))

                    }
                })
            }

        }
        searchAdapter!!.startListening()
        recycler_friend_list.adapter = searchAdapter
    }

    private fun loadFriendList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object: FirebaseRecyclerAdapter<User, UserViewHolder>(options){
            override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): UserViewHolder {
                val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_user, viewGroup, false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.txt_user_email.text = model.email

                holder.setClick(object : IRecyclerItemClickListener {
                    override fun onItemClickListenere(view: View, position: Int) {
                        Common.trackingUser = model
                        startActivity(Intent(this@HomeActivity, TrackingActivity::class.java))
                    }
                })
            }

        }
        adapter!!.startListening()
        recycler_friend_list.adapter = adapter
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

    override fun onResume() {
        super.onResume()
        if(adapter != null){
            adapter!!.startListening()
        }
        if(searchAdapter != null){
            searchAdapter!!.startListening()
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_find_people -> {
                startActivity(Intent(this, AllPeopleActivity::class.java))
            }
            R.id.nav_add_friend -> {
                startActivity(Intent(this, FriendRequestActivity::class.java))
            }
            R.id.nav_sign_out -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onFireBaseLoadUserDone(lstEmail: List<String>) {
        material_search_bar.lastSuggestions = lstEmail
    }

    override fun onFireBaseLoadFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}