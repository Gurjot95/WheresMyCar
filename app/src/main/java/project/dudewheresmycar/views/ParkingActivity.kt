package project.dudewheresmycar.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityParkingBinding
import project.dudewheresmycar.viewmodel.ParkingActivityViewModel

class ParkingActivity : AppCompatActivity() {
    lateinit var viewModel: ParkingActivityViewModel
    private lateinit var binding: ActivityParkingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ParkingActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_parking)

        binding.yesButton.setOnClickListener {
            addParking()
        }

        binding.noButton.setOnClickListener {
            startActivity(Intent(baseContext, MainActivity::class.java))
        }
    }

    private fun addParking() {
        TODO("Not yet implemented")
        // Get current location
        // Get current time
        // Display map
        // Set map marker for parking location
        // Save all parking data to shared preferences
    }
}