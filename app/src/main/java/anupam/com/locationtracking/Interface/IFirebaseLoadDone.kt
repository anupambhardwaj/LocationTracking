package anupam.com.locationtracking.Interface

interface IFirebaseLoadDone {
    fun onFireBaseLoadUserDone(lstEmail: List<String>)
    fun onFireBaseLoadFailed(message: String)
}