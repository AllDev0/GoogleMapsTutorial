package com.helloworldstudios.googlemapstutorial

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.helloworldstudios.googlemapstutorial.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private var coarseLocationRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener(googleMapListener)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                val currentLocation = LatLng(p0.latitude, p0.longitude)

                mMap.clear()
                mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                try {
                    val addressList = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (!addressList.isNullOrEmpty()){
                        println(addressList[0].toString())
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),coarseLocationRequestCode)
        } else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, locationListener)
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null){
                mMap.addMarker(MarkerOptions().position(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)).title("Last Known Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude), 15f))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == coarseLocationRequestCode){
            if (grantResults.isNotEmpty()){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, locationListener)
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val googleMapListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng) {
            mMap.clear()
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            if (p0 != null){
                var address = ""
                try {
                    val addressList = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (!addressList.isNullOrEmpty()){
                        if (addressList.get(0).thoroughfare != null){
                            address += addressList.get(0).thoroughfare
                            if(addressList.get(0).subThoroughfare != null){
                                address += addressList.get(0).subThoroughfare
                            }
                        }
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                }

                mMap.addMarker(MarkerOptions().position(p0).title(address))
            }
        }

    }
}