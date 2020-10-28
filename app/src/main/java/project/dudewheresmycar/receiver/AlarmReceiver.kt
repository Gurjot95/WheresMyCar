package project.dudewheresmycar.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.text.format.DateFormat
import android.util.Log.d
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import project.dudewheresmycar.R
import project.dudewheresmycar.service.AlarmService
import project.dudewheresmycar.util.Constants
import java.util.*


class AlarmReceiver : BroadcastReceiver() {
    private lateinit var alarmService: AlarmService

    override fun onReceive(context: Context, intent: Intent) {
        var timeInMillis = intent.getLongExtra(Constants.EXTRA_EXACT_ALARM_TIME, 0L)

        alarmService = AlarmService(context)

        when (intent.action) {
            Constants.ACTION_SET_EXACT -> {
                buildNotification(context, "Move your car!", convertDate(timeInMillis))
            }
            else -> {
                val snoozeValue = intent.getIntExtra(Constants.EXTRA_SNOOZE, 0)?.let {
                    if(it != null || it != 0) {
                        val calendar = Calendar.getInstance()
                        timeInMillis = calendar.timeInMillis + (2 * Constants.MIN_TO_MILLI)
                        //timeInMillis = calendar.timeInMillis + (it * Constants.MIN_TO_MILLI)
                        alarmService.setExactAlarm(timeInMillis)

                        d("test>", "snooze " + convertDate(timeInMillis))
                    }
                }
            }
        }
    }

    private fun buildNotification(context: Context, title: String, message: String) {
        // Create and register notification channel api 26+
        val channelId = "channel_id"
        createNotificationChannel(context, channelId)

        val intentAction = Intent(context, AlarmReceiver::class.java).apply {
            //action = Constants.ACTION_SNOOZE
            putExtra(Constants.EXTRA_SNOOZE, 15)
        }

        val pendingIntent =
            PendingIntent.getBroadcast(context, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_car)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_bell, "Snooze 15 minutes" , pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(1, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        // Create the NotificationChannel, but only on API 26+ (Android 8.0) because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val channelDescription = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, name, importance)
            channel.apply {
                description = channelDescription
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun convertDate(timeInMillis: Long): String =
        DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString()

}