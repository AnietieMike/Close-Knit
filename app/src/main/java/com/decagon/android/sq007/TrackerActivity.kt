package com.decagon.android.sq007

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*


class TrackerActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private val MIN_TIME: Long = 1000
    private val MIN_DISTANCE: Float = 1F
    private val REQUEST_PERMISSION_LOCATION = 120

    private lateinit var mMap: GoogleMap
    private lateinit var myMarker: Marker
    private lateinit var dbReference: DatabaseReference
    private lateinit var manager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        dbReference = FirebaseDatabase.getInstance().reference.child("Anietie")

        manager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getLocationUpdates()
        readLocationChanges()
    }

    private fun readLocationChanges() {
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        val location = dataSnapshot.getValue(MyLocation::class.java)
                        if (location != null) {
                            myMarker.position = LatLng(location.latitude, location.longitude)


//                            var userLocation = MyLocation()
//                            dbReference.child("Anietie").setValue()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@TrackerActivity, e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getLocationUpdates() {
        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                when {
                    manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                        manager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME,
                            MIN_DISTANCE,
                            this
                        )
                    }
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                        manager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME,
                            MIN_DISTANCE,
                            this
                        )
                    }
                    else -> {
                        Toast.makeText(this, "No provider currently enabled", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocationUpdates()
            } else {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(6.60, 3.50)
        myMarker = mMap.addMarker(MarkerOptions().position(sydney).title("Anietie"))
//        mMap.uiSettings.isZoomControlsEnabled
//        mMap.uiSettings.setAllGesturesEnabled(true)
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20f), 2000, null)
    }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            saveLocation(location)
        } else {
            Toast.makeText(this, "Couldn't find a location", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveLocation(location: Location) {
        dbReference.setValue(location)
    }
}