package project.dudewheresmycar.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.view.circulartimerview.CircularTimerListener
import com.view.circulartimerview.CircularTimerView
import com.view.circulartimerview.TimeFormatEnum
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityParkingBinding
import project.dudewheresmycar.model.ParkingData
import project.dudewheresmycar.viewmodel.ParkingActivityViewModel
import java.lang.Math.ceil
import java.sql.Time
import java.time.temporal.ChronoUnit.MINUTES
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.math.ceil


class ParkingActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var viewModel: ParkingActivityViewModel
    private lateinit var binding: ActivityParkingBinding
    private lateinit var currentLatLng: LatLng
    private lateinit var parkingLatLng: LatLng
    private lateinit var startTime: LocalTime
    private lateinit var endTime: LocalTime
    private lateinit var map: GoogleMap
    private lateinit var sharedPref: SharedPreferences

    //private lateinit var userCurrentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var isParkingSetup: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ParkingActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_parking)
        isParkingDataSaved()
        if (isParkingSetup)
            showAndSetupParkingView()
        // First Check if parking is currently active from sharedprefs,
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
            mapFragment.getMapAsync(this)
        }

        binding.noButton.setOnClickListener {
            onBackPressed()
            //startActivity(Intent(baseContext, MainActivity::class.java))
        }

        binding.finishBtn.setOnClickListener {
            with(sharedPref.edit().clear()) {
                apply()
            }

            var reminderSharedPref = getSharedPreferences("views.ReminderActivity", Context.MODE_PRIVATE)
            with(reminderSharedPref.edit().clear()) {
                apply()
            }

            isParkingSetup = false
            //showHideViews(false)
            setUpMap()
            Snackbar.make(
                binding.root, "Your parking has been finished!", Snackbar.LENGTH_SHORT
            ).show()
            onBackPressed()

        }

        binding.refreshButton.setOnClickListener {
            setUpMap()
        }

        binding.remindersButton.setOnClickListener {
            startActivity(
                Intent(
                    baseContext,
                    ReminderActivity::class.java
                )
            )
        }


        // Toolbar modifications
        binding.homeToolbar.toolbarLogo.setImageResource(R.drawable.ic_traffic)
        binding.homeToolbar.toolbarLogo.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.homeToolbar.toolbar.background = ContextCompat.getDrawable(
            this,
            R.drawable.semi_circle_cyan
        )
        /*
        binding.homeToolbar.toolbarTitle.text = resources.getString(R.string.parking_title)
        binding.homeToolbar.toolbarDesc.text = resources.getString(R.string.parking_info)
        */
    }

    private fun isParkingDataSaved(): Boolean {
        sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return false
        isParkingSetup = sharedPref.contains("ParkingData")

        return isParkingSetup
    }


    private fun showAndSetupParkingView() {
        // sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return

        //binding.showParkedCarView.visibility = View.VISIBLE
        var parkingData: ParkingData = Gson().fromJson(
            sharedPref.getString("ParkingData", ""),
            ParkingData::class.java
        )
        parkingLatLng = LatLng(parkingData.lat, parkingData.long)

        Log.i("DATA", parkingData.toString())
        startTime = LocalTime.parse(parkingData.startTime)
        endTime = LocalTime.parse(parkingData.endTime)

        showHideViews(true)

        Log.i(
            "TIMER",
            ((MINUTES.between(startTime, LocalTime.now()).toFloat() / MINUTES.between(
                startTime,
                endTime
            ).toFloat()) * 100).toString()
        )

        var progressBar: CircularTimerView = binding.progressCircular

        progressBar.setSuffix(" min");
        progressBar.setCircularTimerListener(
            object : CircularTimerListener {
                override fun updateDataOnTick(remainingTimeInMs: Long): String? {
                    progressBar.setText("Time Left\n")
                    progressBar.progress =
                        (MINUTES.between(startTime, LocalTime.now()).toFloat() / MINUTES.between(
                            startTime,
                            endTime
                        ).toFloat()) * 100
                    return (((remainingTimeInMs / 1000) / 60) + 1).toString()
                }

                override fun onTimerFinished() {
                    progressBar.setPrefix("");
                    progressBar.setSuffix("");
                    progressBar.setText("Finished!");
                    Toast.makeText(this@ParkingActivity, "FINISHED", Toast.LENGTH_SHORT).show()
                }
            },
            MINUTES.between(LocalTime.now(), endTime),
            TimeFormatEnum.MINUTES,
            MINUTES.between(startTime, endTime)
        )
        progressBar.startTimer()
    }

    private fun showHideViews(isParking: Boolean) {
        binding.setupLocation.visibility = View.VISIBLE
        binding.setupTime.visibility = View.GONE

        binding.homeToolbar.toolbarTitle.text =
            if (isParking) "Current Parking Details" else resources.getString(R.string.parking_title)
        binding.homeToolbar.toolbarDesc.text =
            if (isParking) "${getAddress(parkingLatLng)}\n${parkingLatLng.latitude}, ${parkingLatLng.longitude}\n${startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}" else resources.getString(R.string.parking_info)
        binding.addressLine.text =
            if (!isParking) getAddress(parkingLatLng) else binding.addressLine.text
        binding.locationInfo.text =
            if (isParking) "${startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}\nTime remaining for your parking" else resources.getString(
                R.string.location_info
            )

        binding.progressCircular.visibility = if (isParking) View.VISIBLE else View.GONE
        binding.yesButton.visibility = if (isParking) View.GONE else View.VISIBLE
        binding.noButton.visibility = if (isParking) View.GONE else View.VISIBLE
        binding.refreshButton.visibility = if (isParking) View.VISIBLE else View.GONE
        binding.remindersButton.visibility = if (isParking) View.VISIBLE else View.GONE
        binding.finishBtn.visibility = if (isParking) View.VISIBLE else View.GONE
    }

    // Update startTime and endTime
    private fun addParkingTime() {
        binding.setupLocation.visibility = View.GONE;
        binding.setupTime.visibility = View.VISIBLE;
        binding.startTime.text = LocalTime.now().format(
            DateTimeFormatter.ofLocalizedTime(
                FormatStyle.SHORT
            )
        ).toString()
        binding.endTime.text = LocalTime.now().plusHours(2).format(
            DateTimeFormatter.ofLocalizedTime(
                FormatStyle.SHORT
            )
        ).toString()
        startTime = LocalTime.parse(binding.startTime.text, DateTimeFormatter.ofPattern("h:mm a"))
        endTime = LocalTime.parse(binding.endTime.text, DateTimeFormatter.ofPattern("h:mm a"))
        /*
        Log.i("START TIME", startTime.format(DateTimeFormatter.ofPattern("h:mm a")))
        endTime = LocalTime.parse(binding.endTime.text, DateTimeFormatter.ofPattern("h:mm a"))
        Log.i("END TIME", endTime.format(DateTimeFormatter.ofPattern("h:mm a")))
         */
        binding.startTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val startTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.startTime.text = SimpleDateFormat("h:mm a").format(cal.time)
                startTime = LocalTime.parse(
                    binding.startTime.text,
                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                )
                //Log.i("START TIME", startTime.format(DateTimeFormatter.ofPattern("h:mm a")))
            }
            TimePickerDialog(
                this, startTimeSetListener, startTime.hour, startTime.minute, false
            ).show()
        }
        binding.endTime.setOnClickListener() {
            val cal = Calendar.getInstance()
            val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.endTime.text = SimpleDateFormat("h:mm a").format(cal.time)
                endTime = LocalTime.parse(
                    binding.endTime.text,
                    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                )
                //Log.i("END TIME", endTime.format(DateTimeFormatter.ofPattern("h:mm a")))
            }
            TimePickerDialog(
                this, endTimeSetListener, endTime.hour, endTime.minute, false
            ).show()
        }
    }


    private fun saveParkingInfo() {
        //TODO Save all parking data to shared preferences once time and location is setup
        //Get appropriate values and pass it in these parameters, I am using dummy values for now
        var parkingDataString = Gson().toJson(
            ParkingData(
                currentLatLng.latitude, currentLatLng.longitude,
                getAddress(currentLatLng),
                startTime.toString(),
                endTime.toString()
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
                map.clear();
                //userCurrentLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                val results = FloatArray(1)
                if (isParkingSetup) Location.distanceBetween(
                    parkingLatLng.latitude, parkingLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, results
                )
                binding.addressLine.text =
                    if (isParkingSetup) "Your car is parked at " + getAddress(
                        parkingLatLng
                    ) + " which is " + results[0] + " meter away from you!" else getAddress(
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
}