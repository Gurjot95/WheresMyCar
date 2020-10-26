package project.dudewheresmycar.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityParkingBinding
import project.dudewheresmycar.model.ParkingData
import project.dudewheresmycar.viewmodel.ParkingActivityViewModel
import java.text.DecimalFormat
import java.util.*


class ParkingActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var viewModel: ParkingActivityViewModel
    private lateinit var binding: ActivityParkingBinding
    private lateinit var currentLatLng: LatLng
    private lateinit var parkingLatLng: LatLng
    private lateinit var map: GoogleMap
    private lateinit var sharedPref: SharedPreferences

    //private lateinit var userCurrentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var isParkingSetup: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ParkingActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_parking)
        isParkingDataSaved()
        if (isParkingSetup)
            showAndSetupParkingView()
        //First Check if parking is currently active from sharedprefs,
        // if yes show parking view otherwise let user decide if he wants to set current location

        val mapFragment = supportFragmentManager
            .findFragmentById(
                R.id.mapView
            ) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.yesButton.setOnClickListener {
            addParkingTime()
        }

        binding.confirmParking.setOnClickListener {
            saveParkingInfo()
        }

        binding.noButton.setOnClickListener {
            onBackPressed()
            //startActivity(Intent(baseContext, MainActivity::class.java))
        }

        binding.finishBtn.setOnClickListener {
            with(sharedPref.edit().clear()) {
                apply()
            }
            isParkingSetup = false
            showHideViews(false)
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

    private fun isParkingDataSaved(): Boolean {
        sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return false
        isParkingSetup = sharedPref.contains("ParkingData")

        return isParkingSetup

    }


    private fun showAndSetupParkingView() {
        // sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return

        //binding.showParkedCarView.visibility = View.VISIBLE
        showHideViews(true)
        var parkingData: ParkingData = Gson().fromJson(
            sharedPref.getString("ParkingData", ""),
            ParkingData::class.java
        )
        parkingLatLng = LatLng(parkingData.lat, parkingData.long)
    }

    private fun showHideViews(isParking: Boolean) {
        binding.setupLocation.visibility = View.VISIBLE
        binding.setupTime.visibility = View.GONE

        binding.progressCircular.visibility = if (isParking) View.VISIBLE else View.GONE
        binding.yesButton.visibility = if (isParking) View.GONE else View.VISIBLE
        binding.noButton.visibility = if (isParking) View.GONE else View.VISIBLE
        binding.finishBtn.visibility = if (isParking) View.VISIBLE else View.GONE
    }


    private fun addParkingTime() {
        binding.setupLocation.visibility = View.GONE;
        binding.setupTime.visibility = View.VISIBLE;

    }

    private fun saveParkingInfo() {
        //TODO Save all parking data to shared preferences once time and location is setup
        //Get appropriate values and pass it in these parameters, I am using dummy values for now
        val parkingDataString = Gson().toJson(
            ParkingData(
                currentLatLng.latitude,
                currentLatLng.longitude, getAddress(currentLatLng),
                Date(),
                Date()
            )
        )
        //val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("ParkingData", parkingDataString)
            apply()
        }

        Snackbar.make(
            binding.root, "Parking Data has been saved", Snackbar.LENGTH_SHORT
        ).show()
        // onBackPressed()
        showAndSetupParkingView()
    }

    private fun getAddress(latLng: LatLng): String {
        val addresses: List<Address>
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


        return addresses[0].getAddressLine(0)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupPermissionsAndMap()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setupPermissionsAndMap() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access the GPS is required to setup parking")
                    .setTitle("Permission required")
                builder.setPositiveButton(
                    "OK"
                ) { dialog, id ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else
            setUpMap()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    //Log.i(TAG, "Permission has been denied by user")
                } else {
                    setUpMap()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpMap() {
        map.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {

                //userCurrentLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                val results = FloatArray(1)
                Location.distanceBetween(
                    parkingLatLng.latitude, parkingLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, results
                )
                binding.addressLine.text = if(isParkingSetup) "Your car is parked at "+getAddress(
                    parkingLatLng
                ) + " which is "+results[0]+" meter away from you!" else getAddress(
                    currentLatLng
                )
                when (isParkingSetup) {
                    true -> {
                        placeMarkerOnMap(parkingLatLng)
                    }
                    false -> {
                        placeMarkerOnMap(currentLatLng)
                    }
                }
                val builder: LatLngBounds.Builder = LatLngBounds.Builder()
                if (isParkingSetup) builder.include(parkingLatLng)
                builder.include(currentLatLng)
                val bounds = builder.build()
                val width = resources.displayMetrics.widthPixels
                val height = resources.displayMetrics.heightPixels
                val padding = (width * 0.15).toInt()
                val cu: CameraUpdate =
                    CameraUpdateFactory.newLatLngBounds(bounds, padding)
                map.animateCamera(cu)
            }
        }
    }


    private fun placeMarkerOnMap(location: LatLng): MarkerOptions {
        val markerOptions = MarkerOptions().position(location).title("Parking")
            .icon(getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.ic_car)))
        map.addMarker(markerOptions)
        map.setOnMarkerClickListener(this)
        return markerOptions
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMarkerClick(p0: Marker?) = false

    fun CalculationByDistance(StartP: LatLng, EndP: LatLng): Int {
        val Radius = 6371 // radius of earth in Km
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2)))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val km = valueResult / 1
        val newFormat = DecimalFormat("####")
        val kmInDec: Int = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec: Int = Integer.valueOf(newFormat.format(meter))
        return meterInDec
    }
}