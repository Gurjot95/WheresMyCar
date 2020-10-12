package project.dudewheresmycar.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_reminder.*
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityReminderBinding
import project.dudewheresmycar.service.AlarmService
import project.dudewheresmycar.viewmodel.ReminderActivityViewModel
import java.util.*

class ReminderActivity : AppCompatActivity() {
    lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding
    lateinit var alarmService: AlarmService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        viewModel = ViewModelProviders.of(this).get(ReminderActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder)

        //You can use below code for future and avoid creating multiple drawables to change color
        //var shape = ContextCompat.getDrawable(this,R.drawable.semi_circle) as LayerDrawable
        //shape.getDrawable(R.id.semiCircleColor).colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this,R.color.orange), PorterDuff.Mode.SRC_ATOP)

        // Toolbar modifications
        binding.homeToolbar.toolbar.background = ContextCompat.getDrawable(this,R.drawable.semi_circle_orange)
        binding.homeToolbar.toolbarLogo.setImageResource(R.drawable.ic_bell)
        binding.homeToolbar.toolbarLogo.setColorFilter(ContextCompat.getColor(this,R.color.white))
        binding.homeToolbar.toolbarTitle.text = resources.getString(R.string.reminder_title)
        binding.homeToolbar.toolbarDesc.text = resources.getString(R.string.reminder_info)

        /* TODO:
        * Remove reminder / remove pending intent
        * Replace calendar value w/ exact alarm value
        */

        //
        alarmService = AlarmService(this)

        reminderBtn.setOnClickListener {
            setAlarm { alarmService.setExactAlarm(it) }
        }
    }

    private fun setAlarm(callback: (Long) -> Unit) {
        Calendar.getInstance().apply {
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            DatePickerDialog(
                this@ReminderActivity,
                0,
                { _, year, month, day ->
                    this.set(Calendar.YEAR, year)
                    this.set(Calendar.MONTH, month)
                    this.set(Calendar.DAY_OF_MONTH, day)
                    TimePickerDialog(
                        this@ReminderActivity,
                        0,
                        { _, hour, minute ->
                            this.set(Calendar.HOUR_OF_DAY, hour)
                            this.set(Calendar.MINUTE, minute)
                            callback(this.timeInMillis)
                        },
                        this.get(Calendar.HOUR_OF_DAY),
                        this.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                this.get(Calendar.YEAR),
                this.get(Calendar.MONTH),
                this.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}