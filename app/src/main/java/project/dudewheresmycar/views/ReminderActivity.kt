package project.dudewheresmycar.views

import android.os.Bundle
import android.util.Log.d
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_reminder.*
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityReminderBinding
import project.dudewheresmycar.service.AlarmService
import project.dudewheresmycar.viewmodel.ReminderActivityViewModel
import java.util.*
import kotlin.properties.Delegates


class ReminderActivity : AppCompatActivity() {
    lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding
    lateinit var alarmService: AlarmService
    var reminderEnabled = false
    val MINUTES_10 = 10
    val MINUTES_15 = 15
    val MINUTES_25 = 25
    val MINUTES_30 = 30
    val MINUTES_45 = 45
    val MINUTES_60 = 60

    private var selectedTime by Delegates.observable(0) { _, oldValue, newValue ->
        d("test>", "selectedTime $oldValue->$newValue")

        if (newValue in 1..60) {
            val calendar = Calendar.getInstance()
            val curTimeInMillis = calendar.timeInMillis + (60000 * newValue) // 1 min = 60000 mills

            alarmService.setExactAlarm(curTimeInMillis)
        } else if (newValue == 0 && oldValue != 0) {
            createSnackBar(
                getString(
                    R.string.timer_disabled,
                    newValue.toString()
                )
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
    }

    private fun setState() {
        //TODO Additionally, you can refactor the entire if-else statement below to just 3-4 lines. I want you to think of a way to do this.
        // Hint: Use teneray operators to change values according to condition and use already defined methods to execute same code
        if (reminderEnabled) {
            reminderStatus.text = getString(R.string.disable)
            reminderBtn.text = getString(R.string.enable_reminder)
            timerOptions1.clearCheck()
            timerOptions2.clearCheck()
            cancelReminder()

            for (i in 0 until timerOptions1.childCount) {
                (timerOptions1.getChildAt(i) as RadioButton).isEnabled = false
                (timerOptions2.getChildAt(i) as RadioButton).isEnabled = false
            }
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
            //TODO Julien: Use constant values 10,15 etc as final variables at the top of class.
            // Also Avoid Checking by ID in favor for views in future as its getting deprecated
            if (checkedId != -1) {
                when (checkedId) {
                    R.id.tenMinutes -> {
                        selectedTime = MINUTES_10
                    }
                    R.id.fifteenMinutes -> {
                        selectedTime = MINUTES_15
                    }
                    R.id.twentyFiveMinutes -> {
                        selectedTime = MINUTES_25
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
            //TODO Julien: Similarly here as well!
            if (checkedId != -1) {
                when (checkedId) {
                    R.id.thirtyMinutes -> {
                        selectedTime = MINUTES_30
                    }
                    R.id.fortyFiveMinutes -> {
                        selectedTime = MINUTES_45
                    }
                    R.id.sixtyMinutes -> {
                        selectedTime = MINUTES_60
                    }
                }
                createSnackBar(
                    getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    )
                )

                timerOptions1.setOnCheckedChangeListener(null)
                timerOptions1.clearCheck()
                timerOptions1.setOnCheckedChangeListener(listener1)
            }
        }

    private fun cancelReminder() {
        selectedTime = 0
    }
}