package anupam.com.locationtracking.Model

class MyLocation {
    var accuracy: Int = 0
    var altitude: Int = 0
    var bearing: Int = 0
    var bearingAccuracyDegrees: Int = 0
    var speed: Int = 0
    var speedAccuracyMetersPerSeconds: Int = 0
    var verticalAccuracyMeters: Int = 0
    var isComplete: Boolean = false
    var isFromMockProvider: Boolean = false
    var provider: String? = null
    var time: Long = 0
    var elapsedRealtimeNanos: Long = 0
    var latitude: Double = 0.toDouble()
    var longtitude: Double = 0.toDouble()
}