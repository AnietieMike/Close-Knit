package com.decagon.android.sq007

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*


//

class TrackerActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_PERMISSION_LOCATION = 120

    private lateinit var mMap: GoogleMap
    var myMarker: Marker? = null
    var myMarker2: Marker? = null
    lateinit var dbReference: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private fun accessLocationServices() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mMap.isMyLocationEnabled = true
            getLocationUpdates()
            readLocationChanges()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                accessLocationServices()
            } else {
                Toast.makeText(this, "Access Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        dbReference = FirebaseDatabase.getInstance().reference

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        dbReference.addValueEventListener(locationLogging)

    }

    private val locationLogging = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val userLocation = snapshot.child("Michael").getValue(UserLocation::class.java)
                var userLat = userLocation?.latitude
                var userLong = userLocation?.longitude

                if (userLat != null && userLong != null) {
                    val user = LatLng(userLat, userLong)

//                    mMap.clear()
//                    myMarker2 = mMap.addMarker(MarkerOptions().position(user).title("Michael"))
                    Log.d("TAG", "onDataChange: $myMarker")
//                    mMap.addMarker(myMarker)
                    myMarker2?.position = user
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 19F))

//                    Toast.makeText(
//                        applicationContext,
//                        "Location accessed from database",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            }
        }

    override fun onCancelled(error: DatabaseError) {
            Toast.makeText(this@TrackerActivity, "Could not read from database", Toast.LENGTH_LONG)
                .show()
        }

    }

    @SuppressLint("MissingPermission")
    private fun readLocationChanges() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 3000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationUpdate: LocationResult?) {
                if (locationUpdate?.locations!!.isNotEmpty()) {
                    val location = locationUpdate?.lastLocation

                    val myLocation = UserLocation(location.latitude, location.longitude)
                    dbReference.child("Anietie").setValue(myLocation)
                        .addOnSuccessListener {
//                            Toast.makeText(applicationContext, "Locations written into the database", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                        }

                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)

                        /*
                        ** To create a marker to indicate a User on the map
                         */
                        val markerOptions = MarkerOptions()
                        markerOptions.position(latLng)
                        markerOptions.title("Anietie")
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_VIOLET))

                        if(myMarker == null) { // Add marker and move camera on first time
                            myMarker = mMap.addMarker(markerOptions)
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20F))
                        } else { // Update existing marker position and move camera if required
                            myMarker!!.position = latLng
//                          mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20F))
                        }

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19F))
                    }

                }
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        accessLocationServices()
        mMap = googleMap
        myMarker2 = mMap.addMarker(MarkerOptions().position(LatLng(6.4741107,3.6304405)).title("Michael"))

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
//        mMap.uiSettings.isMyLocationButtonEnabled = true
    }

}