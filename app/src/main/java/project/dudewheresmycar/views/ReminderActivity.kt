package project.dudewheresmycar.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log.d
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_reminder.*
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityReminderBinding
import project.dudewheresmycar.model.ParkingData
import project.dudewheresmycar.receiver.AlarmReceiver
import project.dudewheresmycar.service.AlarmService
import project.dudewheresmycar.util.Constants
import project.dudewheresmycar.viewmodel.ReminderActivityViewModel
import java.util.*
import kotlin.properties.Delegates

class ReminderActivity : AppCompatActivity() {
    private lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding
    private lateinit var alarmService: AlarmService
    private lateinit var sharedPref: SharedPreferences
    lateinit var parkingData: ParkingData
    var isParkingSetup: Boolean = false
    var reminderEnabled: Boolean = false
    var alarmTime: Long = 0

    private var selectedTime by Delegates.observable(0) { _, oldValue, newValue ->

        if (newValue in 1..60) {
            sharedPref = getSharedPreferences("views.ParkingActivity", Context.MODE_PRIVATE)

            if(sharedPref != null){
                parkingData = Gson().fromJson(
                    sharedPref.getString("ParkingData", ""),
                    ParkingData::class.java
                )

                val calendar = Calendar.getInstance()

                // TODO: parkingTime Convert to millis
                // val parkingTime = parkingData.endTime
                // val parkingTime = calendar.timeInMillis + (Constants.MIN_TO_MILLI * 60) // this is 1hr
                // val curTimeInMillis = parkingTime - (Constants.MIN_TO_MILLI * newValue) // 1hr - selected, NOTE: 1 min = 60000 mills

                val SAMPLEPARKINGTIME = calendar.timeInMillis + (Constants.MIN_TO_MILLI * 1) // this is 1hr
                val curTimeInMillis = SAMPLEPARKINGTIME - (Constants.MIN_TO_MILLI * 1) // 1hr - selected, NOTE: 1 min = 60000 mills

                /*with(sharedPref.edit()) {
                    putString("ParkingData", parkingDataString)
                    apply()
                }*/

                alarmTime = curTimeInMillis
                alarmService.setExactAlarm(curTimeInMillis)

                d("test>", "alarmTime is " + convertDate(alarmTime))

            } else {
                cancelReminder();
            }
        } else if (newValue == 0 && oldValue != 0) {
            createSnackBar(
                getString(R.string.timer_disabled)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        viewModel = ViewModelProviders.of(this).get(ReminderActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder)

        //You can use below code for future and avoid creating multiple drawables to change color
        //var shape = ContextCompat.getDrawable(this,R.drawable.semi_circle) as LayerDrawable
        //shape.getDrawable(R.id.semiCircleColor).colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this,R.color.orange), PorterDuff.Mode.SRC_ATOP)

        // Toolbar modifications
        binding.homeToolbar.toolbar.background =
            ContextCompat.getDrawable(this, R.drawable.semi_circle_orange)
        binding.homeToolbar.toolbarLogo.setImageResource(R.drawable.ic_bell)
        binding.homeToolbar.toolbarLogo.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.homeToolbar.toolbarTitle.text = resources.getString(R.string.reminder_title)
        binding.homeToolbar.toolbarDesc.text = resources.getString(R.string.reminder_info)

        alarmService = AlarmService(this)

        setState()

        setUpRadioGroup()

        reminderBtn.setOnClickListener {
            reminderEnabled = !reminderEnabled
            setState()
        }

        var sn = this.intent.getIntExtra( Constants.EXTRA_SNOOZE, 0)
        d("test>" , "snooze$sn")
    }

    private fun isParkingDataSaved(): Boolean {
        sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return false
        isParkingSetup = sharedPref.contains("ParkingData")
        return isParkingSetup
    }

    private fun setState() {
        // TODO Additionally, you can refactor the entire if-else statement below to just 3-4 lines. I want you to think of a way to do this.
        // Hint: Use teneray operators to change values according to condition and use already defined methods to execute same code
        if (reminderEnabled) {
            reminderStatus.text = getString(R.string.disable)
            reminderBtn.text = getString(R.string.enable_reminder)
            timerOptions1.clearCheck()
            timerOptions2.clearCheck()

            for (i in 0 until timerOptions1.childCount) {
                (timerOptions1.getChildAt(i) as RadioButton).isEnabled = false
                (timerOptions2.getChildAt(i) as RadioButton).isEnabled = false
            }

            cancelReminder()
        } else { // if enabled
            reminderStatus.text = getString(R.string.enable)
            reminderBtn.text = getString(R.string.disable_reminder)

            for (i in 0 until timerOptions1.childCount) {
                (timerOptions1.getChildAt(i) as RadioButton).isEnabled = true
                (timerOptions2.getChildAt(i) as RadioButton).isEnabled = true
            }
        }
    }

    /*
    * A work around for displaying radioGroup buttons in 2 rows,
    * used two radioGroups
    * see: https://stackoverflow.com/questions/10425569/radiogroup-with-two-columns-which-have-ten-radiobuttons
    * */
    private fun setUpRadioGroup() {
        timerOptions1.clearCheck()
        timerOptions2.clearCheck()
        timerOptions1.setOnCheckedChangeListener(listener1)
        timerOptions2.setOnCheckedChangeListener(listener2)
    }

    private val listener1: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                when (checkedId) {
                    R.id.tenMinutes -> {
                        selectedTime = Constants.MINUTES_10
                    }
                    R.id.fifteenMinutes -> {
                        selectedTime = Constants.MINUTES_15
                    }
                    R.id.twentyFiveMinutes -> {
                        selectedTime = Constants.MINUTES_25
                    }
                }
                createSnackBar(
                    getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    )
                )
                timerOptions2.setOnCheckedChangeListener(null)
                timerOptions2.clearCheck()
                timerOptions2.setOnCheckedChangeListener(listener2)
            }
        }

    private fun createSnackBar(msg: String) {
        Snackbar.make(
            binding.root, msg, Snackbar.LENGTH_SHORT
        ).show()

    }

    private val listener2: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                when (checkedId) {
                    R.id.thirtyMinutes -> {
                        selectedTime = Constants.MINUTES_30
                    }
                    R.id.fortyFiveMinutes -> {
                        selectedTime = Constants.MINUTES_45
                    }
                    R.id.sixtyMinutes -> {
                        selectedTime = Constants.MINUTES_60
                    }
                }
                createSnackBar(
                    getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    ) + " - " + convertDate(alarmTime)
                )

                timerOptions1.setOnCheckedChangeListener(null)
                timerOptions1.clearCheck()
                timerOptions1.setOnCheckedChangeListener(listener1)
            }
        }

    private fun cancelReminder() {
        alarmService.cancelAlarm()
        selectedTime = 0
    }

    private fun convertDate(timeInMillis: Long): String =
        DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString()

}