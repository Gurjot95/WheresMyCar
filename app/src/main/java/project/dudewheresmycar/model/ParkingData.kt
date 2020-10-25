package project.dudewheresmycar.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class ParkingData(val lat: Double, val long: Double, val address: String,val startTime: Date, val endTime: Date):
    Parcelable {

}