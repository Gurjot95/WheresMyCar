package project.dudewheresmycar.views

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
import kotlinx.android.synthetic.main.toolbar.view.*
import project.dudewheresmycar.R
import project.dudewheresmycar.databinding.ActivityReminderBinding
import project.dudewheresmycar.viewmodel.ReminderActivityViewModel


class ReminderActivity : AppCompatActivity() {
    lateinit var viewModel: ReminderActivityViewModel
    private lateinit var binding: ActivityReminderBinding

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
    }
}