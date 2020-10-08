package project.dudewheresmycar.views

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import project.dudewheresmycar.R

class MainActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Call ParkingActivity
        findViewById<Button>(R.id.setupParkingBtn).setOnClickListener(){
            val intent = Intent(this, ParkingActivity::class.java).apply{
            }
            startActivityForResult(intent, 0)
        }

        // Call ReminderActivity
        findViewById<Button>(R.id.setupReminderBtn).setOnClickListener(){
            val intent = Intent(this, ReminderActivity::class.java).apply{
            }
            startActivityForResult(intent, 0)
        }
    }
}