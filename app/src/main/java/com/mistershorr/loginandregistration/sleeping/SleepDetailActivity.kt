package com.mistershorr.loginandregistration.sleeping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mistershorr.loginandregistration.SleepAdapter
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Date

class SleepDetailActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SleepDetailActivity"
        const val EXTRA_SLEEP = "sleepyTime"
    }
    private lateinit var binding: ActivitySleepDetailBinding
    lateinit var bedTime: LocalDateTime
    lateinit var wakeTime: LocalDateTime
    private var sleep: Sleep? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sleep = intent.getParcelableExtra(EXTRA_SLEEP)
        if(sleep == null) {
            val zoneId = ZoneId.systemDefault()
            bedTime = LocalDateTime.now()
            wakeTime = bedTime.plusHours(8)

            binding.buttonSleepDetailSave.setOnClickListener {
                Log.d(TAG, "try to save new")

                val newSleepRecord = Sleep(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(binding.buttonSleepDetailWakeTime.drawingTime), zoneId)
                        .toInstant(UTC)
                        .toEpochMilli(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(binding.buttonSleepDetailBedTime.drawingTime), zoneId)
                        .toInstant(UTC)
                        .toEpochMilli(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(binding.buttonSleepDetailBedTime.drawingTime), zoneId)
                        .toLocalDate()
                        .atStartOfDay()
                        .toInstant(UTC)
                        .toEpochMilli(),
                    binding.ratingBarSleepDetailQuality.rating.toInt(),
                    binding.editTextTextMultiLineSleepDetailNotes.text.toString(),
                    Backendless.UserService.CurrentUser().userId
                )
                addSleepRecord(newSleepRecord)
            }
        } else {
            var instant = Instant.ofEpochMilli(sleep!!.bedMillis)
            val zoneId = ZoneId.systemDefault()
            bedTime = instant.atZone(zoneId).toLocalDateTime()

            instant = Instant.ofEpochMilli(sleep!!.wakeMillis)
            wakeTime = instant.atZone(zoneId).toLocalDateTime()

            binding.ratingBarSleepDetailQuality.rating = sleep!!.quality.toFloat()
            binding.editTextTextMultiLineSleepDetailNotes.setText(sleep!!.notes)

            binding.buttonSleepDetailSave.setOnClickListener {
                Log.d(TAG, "try to update")
                val newSleepRecord = Sleep(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(binding.buttonSleepDetailWakeTime.drawingTime), zoneId)
                        .toInstant(UTC)
                        .toEpochMilli(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(binding.buttonSleepDetailBedTime.drawingTime), zoneId)
                        .toInstant(UTC)
                        .toEpochMilli(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(binding.buttonSleepDetailBedTime.drawingTime), zoneId)
                        .toLocalDate()
                        .atStartOfDay()
                        .toInstant(UTC)
                        .toEpochMilli(),
                    binding.ratingBarSleepDetailQuality.rating.toInt(),
                    binding.editTextTextMultiLineSleepDetailNotes.text.toString(),
                    Backendless.UserService.CurrentUser().userId
                )
                updateSleepRecord(newSleepRecord)
            }
        }

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
        binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy")
        binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)

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

    private fun updateSleepRecord(sleep: Sleep) {
        val sleepRecord = sleep
        sleepRecord.ownerId = Backendless.UserService.CurrentUser().userId

        Backendless.Data.of(Sleep::class.java).save(sleepRecord, object : AsyncCallback<Sleep> {
            override fun handleResponse(sleepRecord: Sleep) {
                // set new fields based on user input
                // sleepRecord.sleepDate = binding.dateInput.date
                Backendless.Data.of(Sleep::class.java)
                    .save(sleepRecord, object : AsyncCallback<Sleep?> {
                        override fun handleResponse(response: Sleep?) {
                            Log.d(SleepListActivity.TAG, "response: $response")
                            finish()
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            Log.d(SleepListActivity.TAG, "handleFault: ${fault.message}")
                        }
                    })
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(SleepListActivity.TAG, "handleFault: ${fault.message}")
            }
        })
    }

    fun setTime(time: LocalDateTime, timeFormatter: DateTimeFormatter, button: Button) {
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
            when (button.id) {
                binding.buttonSleepDetailBedTime.id -> {
                    bedTime = selectedTime
                    if (wakeTime.toEpochSecond(UTC) < selectedTime.toEpochSecond(UTC)) {
                        wakeTime = wakeTime.plusDays(1)
                    }
                }

                binding.buttonSleepDetailWakeTime.id -> {
                    if (selectedTime.toEpochSecond(UTC) < bedTime.toEpochSecond(UTC)) {
                        selectedTime = selectedTime.plusDays(1)
                    }
                    wakeTime = selectedTime
                }
            }
        }
    }
}