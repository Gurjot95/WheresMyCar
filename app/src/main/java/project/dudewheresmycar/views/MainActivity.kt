package project.dudewheresmycar.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityMainBinding
import project.dudewheresmycar.model.ParkingData
import project.dudewheresmycar.viewmodel.MainActivityViewModel


class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref:SharedPreferences
    private lateinit var parkingData:ParkingData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        
        binding.setupParkingBtn.setOnClickListener {
            startActivity(Intent(baseContext, ParkingActivity::class.java))
        }

        binding.setupReminderBtn.setOnClickListener {
            startActivity(
                Intent(
                    baseContext,
                    ReminderActivity::class.java
                )
            )
        }

        sharedPref = getSharedPreferences("views.ParkingActivity", Context.MODE_PRIVATE)

        if (sharedPref == null)
            binding.setupReminderBtn.setEnabled(false)
    }

}