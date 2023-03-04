package com.crop2cash.mapsapp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.crop2cash.mapsapp.databinding.ActivityMapsBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mLocationRequest: LocationRequest

    private lateinit var mLastLocation: Location
    private var mCurrLocationMarker: Marker? = null
    private val lstLatLng: MutableList<LatLng> = mutableListOf()
    private val lstMarkers: MutableList<Marker> = mutableListOf()


    private lateinit var mLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMap()
        mLocationClient = FusedLocationProviderClient(this)

        binding.btnDrawLine.setOnClickListener {
            val lagos = LatLng(6.5244, 3.3792)
            val ibadan = LatLng(7.3775, 3.9470)
            val polyline = mMap.addPolyline(PolylineOptions().add(lagos, ibadan))
            polyline.width = 5f
            polyline.color = Color.RED
        }

        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        mLocationClient.lastLocation.addOnCompleteListener {task ->
            if(task.isSuccessful){
                val location = task.result
                goToLocation(location.latitude, location.longitude)

                Log.d("MapsApp", "getCurrentLocation: $location")
                Log.d("MapsApp", "getCurrentLocation: ${location.accuracy}")
            }
        }
    }

    private fun goToLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions().position(latLng).title("My location")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mCurrLocationMarker = mMap.addMarker(markerOptions)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 20f)
        mMap.moveCamera(cameraUpdate)
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        drawNewLine(latLng)

    }

    private fun drawNewLine(latLng: LatLng) {
        if(!lstLatLng.isNullOrEmpty()){
            val lastLatLng = lstLatLng.last()
            val polyline = mMap.addPolyline(PolylineOptions().add(lastLatLng, latLng))
            polyline.width = 5f
            polyline.color = Color.YELLOW
        }
        lstLatLng.add(latLng)
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
        setCurrentLocation()
    }

    private fun setCurrentLocation() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                buildGoogleApiClient()
                mMap.isMyLocationEnabled = true
            }
            else
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        }
        else{
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true
        }
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient?.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if(ContextCompat.checkSelfPermission(this,
            ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient!!, mLocationRequest, this)
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 20f)
        mMap.moveCamera(cameraUpdate)
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN


        //stop location update
        if(mGoogleApiClient!=null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient!!, this)



    }
}