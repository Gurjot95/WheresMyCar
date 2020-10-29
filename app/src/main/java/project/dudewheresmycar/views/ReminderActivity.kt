package project.dudewheresmycar.views

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log.d
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_reminder.*
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityReminderBinding
import project.dudewheresmycar.model.ParkingData
import project.dudewheresmycar.service.AlarmService
import project.dudewheresmycar.util.Constants
import project.dudewheresmycar.viewmodel.ReminderActivityViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class ReminderActivity : AppCompatActivity() {
    private lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding
    private lateinit var alarmService: AlarmService
    private lateinit var sharedPref: SharedPreferences
    lateinit var parkingData: ParkingData
    private var isParkingSetup: Boolean = false
    private var isReminderDisabled: Boolean = false
    private var alarmTime: Long = 0
    val calendar = Calendar.getInstance()

    private var selectedTime by Delegates.observable(0) { _, oldValue, newValue ->

        if (newValue in 1..60) {
            if(!sharedPref.contains("ParkingData")) return@observable
            parkingData = Gson().fromJson(
                sharedPref.getString("ParkingData", ""),
                ParkingData::class.java
            )


            // get the parking end time from shared prefs
            val endTimeInMillis = getParkingEndTime(parkingData.endTime)
            val curTimeInMillis = endTimeInMillis - (Constants.MIN_TO_MILLI * newValue)

            // NOTE: DATA FOR TESTING
            //val endTimeInMillis = calendar.timeInMillis + (Constants.MIN_TO_MILLI * 60) // this is 1hr
            //val curTimeInMillis = endTimeInMillis - (Constants.MIN_TO_MILLI * newValue) // 1hr - selected, NOTE: 1 min = 60000 mills

            alarmTime = curTimeInMillis
            alarmService.setExactAlarm(alarmTime)

            // save to shared prefs
            with(sharedPref.edit()) {
                putString("alarmTime", alarmTime.toString())
                putInt("reminderTime", newValue)
                apply()
            }

            d("test>", "parking endTime is " + convertToDate(endTimeInMillis))
            d("test>", "alarmTime is " + convertToDate(alarmTime))
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
        sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return

        isReminderDisabled = sharedPref.getBoolean("isReminderDisabled", false)
        //You can use below code for future and avoid creating multiple drawables to change color
        //var shape = ContextCompat.getDrawable(this,R.drawable.semi_circle) as LayerDrawable
        //shape.getDrawable(R.id.semiCircleColor).colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this,R.color.orange), PorterDuff.Mode.SRC_ATOP)

        // Toolbar modifications
        binding.homeToolbar.toolbar.background =
            ContextCompat.getDrawable(this, R.drawable.semi_circle_orange)
        binding.homeToolbar.toolbarLogo.setImageResource(R.drawable.ic_bell)
        //binding.homeToolbar.toolbarLogo.setColorFilter(ContextCompat.getColor(this, R.color.white))
        binding.homeToolbar.toolbarTitle.text = resources.getString(R.string.reminder_title)
        binding.homeToolbar.toolbarDesc.text = resources.getString(R.string.reminder_info)

        alarmService = AlarmService(this)
        setUpRadioGroup()
        setState()



        reminderBtn.setOnClickListener {
            isReminderDisabled = !isReminderDisabled
            with(sharedPref.edit()) {
                putBoolean("isReminderDisabled", isReminderDisabled)
                apply()
            }

            setState()
        }

        var sn = this.intent.getIntExtra(Constants.EXTRA_SNOOZE, 0)
    }

    private fun isParkingDataSaved(): Boolean {
        //sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return false
        isParkingSetup = sharedPref.contains("ParkingData")
        return isParkingSetup
    }

    private fun setState() {
        // TODO Additionally, you can refactor the entire if-else statement below to just 3-4 lines. I want you to think of a way to do this.
        // Hint: Use teneray operators to change values according to condition and use already defined methods to execute same code
        ViewCompat.setBackgroundTintList(
            reminderBtn,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    if (isReminderDisabled) R.color.cyan else R.color.no
                )
            )
        )
        if (isReminderDisabled) {
            reminderStatus.text = getString(R.string.disable)
            reminderBtn.text = getString(R.string.enable_reminder)

            timerOptions1.clearCheck()
            timerOptions2.clearCheck()

            for (i in 0 until timerOptions1.childCount) {
                val  firstGroupBtn = (timerOptions1.getChildAt(i) as RadioButton)
                val  secondGroupBtn = (timerOptions2.getChildAt(i) as RadioButton)
                firstGroupBtn.isEnabled = false
                secondGroupBtn.isEnabled = false

            }

            cancelReminder()
        } else { // if enabled
            reminderStatus.text = getString(R.string.enable)
            reminderBtn.text = getString(R.string.disable_reminder)

            for (i in 0 until timerOptions1.childCount) {
                val  firstGroupBtn = (timerOptions1.getChildAt(i) as RadioButton)
                val  secondGroupBtn = (timerOptions2.getChildAt(i) as RadioButton)
                (timerOptions1.getChildAt(i) as RadioButton).isEnabled = true
                (timerOptions2.getChildAt(i) as RadioButton).isEnabled = true
                firstGroupBtn.isChecked = firstGroupBtn.tag.equals(sharedPref.getString("selectedReminderTag","-1"))
                secondGroupBtn.isChecked = secondGroupBtn.tag.equals(sharedPref.getString("selectedReminderTag","-1"))
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

                with(sharedPref.edit()) {
                    putString("selectedReminderTag", selectedTime.toString())
                    apply()
                }
                createSnackBar(
                    getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    ) + " - " + convertToDate(alarmTime)
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
                with(sharedPref.edit()) {
                    putString("selectedReminderTag", selectedTime.toString())
                    apply()
                }
                createSnackBar(
                    getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    ) + " - " + convertToDate(alarmTime)
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

    private fun convertToDate(timeInMillis: Long): String =
        DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString()

    private fun getParkingEndTime(parkingEndTime: String): Long {
        val dateToday = DateFormat.format("dd/MM/yyyy", calendar.time).toString()
        val strDate = "$dateToday $parkingEndTime"
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
        val oldDate = formatter.parse(strDate)
        return oldDate.time
    }
}