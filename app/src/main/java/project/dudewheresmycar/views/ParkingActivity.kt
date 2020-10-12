package project.dudewheresmycar.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityParkingBinding
import project.dudewheresmycar.viewmodel.ParkingActivityViewModel


class ParkingActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var viewModel: ParkingActivityViewModel
    private lateinit var binding: ActivityParkingBinding

    //private var mapView: MapView? = null
    private var gMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ParkingActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_parking)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
       // binding.mapView.onCreate(savedInstanceState)
        //binding.mapView.getMapAsync(this)

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

    override fun onMapReady(googleMap: GoogleMap?) {
        gMap = googleMap
        gMap?.apply {
            val sydney = LatLng(-33.852, 151.211)
            addMarker(
                MarkerOptions()
                    .position(sydney)
                    .title("Marker in Sydney")
            )
        }
    }
}