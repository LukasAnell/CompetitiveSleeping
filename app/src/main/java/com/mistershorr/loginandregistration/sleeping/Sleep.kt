package com.mistershorr.loginandregistration.sleeping

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Sleep(
    var wakeTime: Date = Date(),
    var sleepTime: Date = Date(),
    var sleepDate: Date = Date(),
    var quality: Int = 5,
    var notes: String? = null,
    var ownerId: String? = null,
    var objectId: String? = null,
): Parcelable
