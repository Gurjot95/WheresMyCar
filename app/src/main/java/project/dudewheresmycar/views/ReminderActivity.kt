package project.dudewheresmycar.views

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_reminder.*
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityReminderBinding
import project.dudewheresmycar.service.AlarmService
import project.dudewheresmycar.viewmodel.ReminderActivityViewModel


class ReminderActivity : AppCompatActivity() {
    lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding
    lateinit var alarmService: AlarmService
    var selectedTime: Int = 0

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

            //TODO Julien: Use a locally stored boolean flag here as well to get the status
            if(reminderBtn.text.toString() == getString(R.string.enable_reminder))
                reminderBtn.text = getString(R.string.disable_reminder)
            else
                reminderBtn.text = getString(R.string.enable_reminder)

            setState()
        }
    }

    private fun setState(){
        // is disabled

        //TODO Julien: Do not compare conditional flags through Strings as these can be changed in future,
        // instead use a boolean value isEnabled locally through SharedPrefs and use that to check condition

        //TODO Additionally, you can refactor the entire if-else statement below to just 3-4 lines. I want you to think of a way to do this.
        // Hint: Use teneray operators to change values according to condition and use already defined methods to execute same code
        if (reminderBtn.text.toString() == getString(R.string.enable_reminder)) {
            reminderStatus.text = getString(R.string.disable)

            timerOptions1.clearCheck()
            timerOptions2.clearCheck()
            for (i in 0 until timerOptions1.childCount) {
                (timerOptions1.getChildAt(i) as RadioButton).isEnabled = false
                (timerOptions2.getChildAt(i) as RadioButton).isEnabled = false
            }
        } else { // if enabled
            reminderStatus.text = getString(R.string.enable)

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
                        selectedTime = 10
                    }
                    R.id.fifteenMinutes -> {
                        selectedTime = 15
                    }
                    R.id.twentyFiveMinutes -> {
                        selectedTime = 25
                    }
                }

                Toast.makeText(
                    applicationContext, getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    ), Toast.LENGTH_SHORT
                ).show()

                timerOptions2.setOnCheckedChangeListener(null)
                timerOptions2.clearCheck()
                timerOptions2.setOnCheckedChangeListener(listener2)
            }
        }

    private val listener2: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { group, checkedId ->
            //TODO Julien: Similarly here as well!
            if (checkedId != -1) {
                when (checkedId) {
                    R.id.thirtyMinutes -> {
                        selectedTime = 30
                    }
                    R.id.fortyFiveMinutes -> {
                        selectedTime = 45
                    }
                    R.id.sixtyMinutes -> {
                        selectedTime = 60
                    }
                }

                Toast.makeText(
                    applicationContext, getString(
                        R.string.timer_selection,
                        selectedTime.toString()
                    ), Toast.LENGTH_SHORT
                ).show()

                timerOptions1.setOnCheckedChangeListener(null)
                timerOptions1.clearCheck()
                timerOptions1.setOnCheckedChangeListener(listener1)
            }
        }

    private fun setAlarm(callback: (Long) -> Unit) {
        /*Calendar.getInstance().apply {
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
        }*/
    }
}