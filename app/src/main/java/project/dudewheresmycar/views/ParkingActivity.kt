package project.dudewheresmycar.views

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import project.dudewheresmycar.R
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar

class ParkingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking)

        // Toolbar modifications
        findViewById<Toolbar>(R.id.toolbar).background = resources.getDrawable(R.drawable.semi_circle_cyan)
        findViewById<ImageView>(R.id.toolbarLogo).setImageResource(R.drawable.ic_traffic)
        findViewById<ImageView>(R.id.toolbarLogo).setColorFilter(resources.getColor(R.color.white))
        findViewById<TextView>(R.id.toolbarTitle).setText(resources.getString(R.string.parking_title))
        findViewById<TextView>(R.id.toolbarDesc).setText(resources.getString(R.string.parking_info))
    }
}