package com.mistershorr.loginandregistration.sleeping

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

class SleepDetailActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SleepDetailActivity"
        const val EXTRA_SLEEP = "sleepyTime"
    }
    private lateinit var binding: ActivitySleepDetailBinding
    private lateinit var bedTime: LocalDateTime
    private lateinit var wakeTime: LocalDateTime
    private var sleep: Sleep? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sleep = intent.getParcelableExtra(EXTRA_SLEEP)

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy")
        val zoneId = ZoneId.systemDefault()

        binding.buttonSleepDetailSave.setOnClickListener {
            if(sleep == null) {
                saveSleep()
            } else {
                updateSleep(sleep!!)
            }
        }

        if(sleep == null) {
            bedTime = LocalDateTime.now()
            wakeTime = bedTime.plusHours(8)

            binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
            binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
            binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
        } else {
            bedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(sleep!!.bedMillis), zoneId)
            wakeTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(sleep!!.wakeMillis), zoneId)

            binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
            binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
            binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
            binding.ratingBarSleepDetailQuality.rating = sleep!!.quality.toFloat()
            binding.editTextTextMultiLineSleepDetailNotes.setText(sleep!!.notes)
        }

        binding.buttonSleepDetailBedTime.setOnClickListener {
            setTime(bedTime, timeFormatter, binding.buttonSleepDetailBedTime)
        }

        binding.buttonSleepDetailWakeTime.setOnClickListener {
            setTime(wakeTime, timeFormatter, binding.buttonSleepDetailWakeTime)
        }

        binding.buttonSleepDetailDate.setOnClickListener {
            val selection = bedTime.toEpochSecond(UTC)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(selection*1000) // requires milliseconds
                .setTitleText("Select a Date")
                .build()

            Log.d(TAG, "onCreate: after build: ${LocalDateTime.ofEpochSecond(datePicker.selection?: 0L, 0, ZoneOffset.UTC)}")
            datePicker.addOnPositiveButtonClickListener { millis ->
                val selectedLocalDate = Instant.ofEpochMilli(millis).atOffset(UTC).toLocalDateTime()
                Toast.makeText(this, "Date is: ${dateFormatter.format(selectedLocalDate)}", Toast.LENGTH_SHORT).show()

                // make sure that waking up the next day if waketime < bedtime is preserved
                var wakeDate = selectedLocalDate

                if(wakeTime.dayOfMonth != bedTime.dayOfMonth) {
                    wakeDate = wakeDate.plusDays(1)
                }

                bedTime = LocalDateTime.of(
                    selectedLocalDate.year,
                    selectedLocalDate.month,
                    selectedLocalDate.dayOfMonth,
                    bedTime.hour,
                    bedTime.minute
                )

                wakeTime = LocalDateTime.of(
                    wakeDate.year,
                    wakeDate.month,
                    wakeDate.dayOfMonth,
                    wakeTime.hour,
                    wakeTime.minute
                )
                binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
            }
            datePicker.show(supportFragmentManager, "datepicker")
        }

        binding.buttonSleepDetailCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveSleep() {
        val zoneId = ZoneId.systemDefault()

        val wakeString = binding.buttonSleepDetailWakeTime.text.toString()
        val bedString = binding.buttonSleepDetailBedTime.text.toString()
        val dateString = binding.buttonSleepDetailDate.text.toString()

        var wakeMillis = Instant.ofEpochMilli(timeToEpoch(wakeString, dateString))
            .atZone(zoneId)
            .toLocalDateTime()
        val bedMillis = Instant.ofEpochMilli(timeToEpoch(bedString, dateString))
            .atZone(zoneId)
            .toLocalDateTime()

        if (wakeMillis.isBefore(bedMillis)) {
            wakeMillis = wakeMillis.plusDays(1)
        }

        val sleep = Sleep(
            wakeMillis
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            bedMillis
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            bedMillis
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            binding.ratingBarSleepDetailQuality.rating.toInt(),
            binding.editTextTextMultiLineSleepDetailNotes.text.toString(),
            Backendless.UserService.CurrentUser().userId
        )

        addSleepRecord(sleep)
    }

    private fun updateSleep(sleep: Sleep) {
        val zoneId = ZoneId.systemDefault()

        val wakeString = binding.buttonSleepDetailWakeTime.text.toString()
        val bedString = binding.buttonSleepDetailBedTime.text.toString()
        val dateString = binding.buttonSleepDetailDate.text.toString()

        var wakeMillis = Instant.ofEpochMilli(timeToEpoch(wakeString, dateString))
            .atZone(zoneId)
            .toLocalDateTime()
        val bedMillis = Instant.ofEpochMilli(timeToEpoch(bedString, dateString))
            .atZone(zoneId)
            .toLocalDateTime()

        if (wakeMillis.isBefore(bedMillis)) {
            wakeMillis = wakeMillis.plusDays(1)
        }

        val newSleep = Sleep(
            wakeMillis
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            bedMillis
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            bedMillis
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli(),
            binding.ratingBarSleepDetailQuality.rating.toInt(),
            binding.editTextTextMultiLineSleepDetailNotes.text.toString(),
            Backendless.UserService.CurrentUser().userId
        )

        updateSleepRecord(sleep, newSleep)
    }

    private fun timeToEpoch(time: String, dateString: String): Long {
        val newTime = "$dateString $time"
        val formatter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy hh:mm a")
        val dateTime = LocalDateTime.parse(newTime, formatter)
        val zonedDateTime = dateTime.atZone(ZoneId.systemDefault())
        return zonedDateTime.toInstant().toEpochMilli()
    }

    private fun addSleepRecord(newRecord: Sleep) {
        newRecord.ownerId = Backendless.UserService.CurrentUser().userId

        // save object asynchronously
        Backendless.Data.of(Sleep::class.java).save(newRecord, object: AsyncCallback<Sleep?> {
            override fun handleResponse(response: Sleep?) {
                Log.d(SleepListActivity.TAG, "response: $response")
                finish()
            }

            override fun handleFault(fault: BackendlessFault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
                Log.d(SleepListActivity.TAG, "handleFault: ${fault.message}")
            }
        })
    }

    private fun updateSleepRecord(sleep: Sleep, newSleep: Sleep) {
        Backendless.Data.of(Sleep::class.java).save(sleep, object: AsyncCallback<Sleep> {
            override fun handleResponse(savedSleep: Sleep) {
                savedSleep.wakeMillis = newSleep.wakeMillis
                savedSleep.bedMillis = newSleep.bedMillis
                savedSleep.sleepDateMillis = newSleep.sleepDateMillis
                savedSleep.quality = binding.ratingBarSleepDetailQuality.rating.toInt()
                savedSleep.notes = binding.editTextTextMultiLineSleepDetailNotes.text.toString()
                Backendless.Data.of(Sleep::class.java)
                    .save(savedSleep, object : AsyncCallback<Sleep?> {
                        override fun handleResponse(response: Sleep?) {
                            Log.d(TAG, "handleResponse: successful update")
                            finish()
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            Log.d(TAG, "handleFault: ${fault.code}")
                        }
                    })
            }
            override fun handleFault(fault: BackendlessFault) {
                Log.d(TAG, "handleFault: ${fault.message}")
            }
        })
    }

    private fun setTime(time: LocalDateTime, timeFormatter: DateTimeFormatter, button: Button) {
        val timePickerDialog = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .build()

        timePickerDialog.show(supportFragmentManager, "bedtime")
        timePickerDialog.addOnPositiveButtonClickListener {
            var selectedTime = LocalDateTime.of(
                time.year,
                time.month,
                time.dayOfMonth,
                timePickerDialog.hour,
                timePickerDialog.minute
            )
            button.text = timeFormatter.format(selectedTime)
            when(button.id) {
                binding.buttonSleepDetailBedTime.id -> {
                    bedTime = selectedTime
                    if(wakeTime.toEpochSecond(UTC) < selectedTime.toEpochSecond(UTC)) {
                        wakeTime = wakeTime.plusDays(1)
                    }
                }

                binding.buttonSleepDetailWakeTime.id -> {
                    if(selectedTime.toEpochSecond(UTC) < bedTime.toEpochSecond(UTC)) {
                        selectedTime = selectedTime.plusDays(1)
                    }
                    wakeTime = selectedTime
                }
            }
        }
    }
}