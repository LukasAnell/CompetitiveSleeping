package com.mistershorr.loginandregistration.sleeping

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import java.util.Date

class SleepDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SLEEP = "sleepList"
    }
    private lateinit var binding: ActivitySleepDetailBinding
    private lateinit var sleep: Sleep
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sleep = intent.getParcelableExtra(EXTRA_SLEEP)!!
        setFields()
        setListeners()
    }

    private fun setListeners() {
        binding.buttonSleepDetailCancel.setOnClickListener {

        }

        binding.buttonSleepDetailSave.setOnClickListener {

        }
    }

    private fun setFields() {
        binding.buttonSleepDetailDate.text = Date(sleep.sleepDateMillis).toString()
        binding.buttonSleepDetailBedTime.text = Date(sleep.bedMillis).toString()
        binding.buttonSleepDetailWakeTime.text = Date(sleep.wakeMillis).toString()
        binding.ratingBarSleepDetailQuality.rating = sleep.quality.toFloat()
        binding.editTextTextMultiLineSleepDetailNotes.setText(sleep.notes)
    }
}