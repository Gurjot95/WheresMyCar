package project.dudewheresmycar.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityParkingBinding
import project.dudewheresmycar.model.ParkingData
import project.dudewheresmycar.viewmodel.ParkingActivityViewModel


class ParkingActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var viewModel: ParkingActivityViewModel
    private lateinit var binding: ActivityParkingBinding

    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ParkingActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_parking)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.yesButton.setOnClickListener {
            addParking()
        }

        binding.noButton.setOnClickListener {
            onBackPressed()
            //startActivity(Intent(baseContext, MainActivity::class.java))
        }

        // Toolbar modifications
        binding.homeToolbar.toolbar.background = ContextCompat.getDrawable(
            this,
            R.drawable.semi_circle_cyan
        )
        binding.homeToolbar.toolbarLogo.setImageResource(R.drawable.ic_traffic)
        binding.homeToolbar.toolbarLogo.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.homeToolbar.toolbarTitle.text = resources.getString(R.string.parking_title)
        binding.homeToolbar.toolbarDesc.text = resources.getString(R.string.parking_info)
    }

    private fun addParking() {
        TODO("Not yet implemented")
        // Get current location
        // Get current time
        // Display map
        // Set map marker for parking location
        // Save all parking data to shared preferences
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setUpMap()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }


    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        map.addMarker(markerOptions)
        map.setOnMarkerClickListener(this)

    }

    override fun onMarkerClick(p0: Marker?) = false
}