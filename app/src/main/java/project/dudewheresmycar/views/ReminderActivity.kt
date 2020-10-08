package project.dudewheresmycar.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import project.dudewheresmycar.R
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar

class ReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        // Toolbar modifications
        findViewById<Toolbar>(R.id.toolbar).background = resources.getDrawable(R.drawable.semi_circle_orange)
        findViewById<ImageView>(R.id.toolbarLogo).setImageResource(R.drawable.ic_bell)
        findViewById<TextView>(R.id.toolbarTitle).setText(resources.getString(R.string.reminder_title))
        findViewById<TextView>(R.id.toolbarDesc).setText(resources.getString(R.string.reminder_info))
    }
}