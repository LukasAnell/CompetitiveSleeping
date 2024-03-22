package com.mistershorr.loginandregistration.sleeping

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.mistershorr.loginandregistration.Constants
import com.mistershorr.loginandregistration.databinding.ActivitySleepListBinding


class SleepListActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SleepListActivity"
    }

    private lateinit var binding: ActivitySleepListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Backendless.initApp(this, Constants.APPLICATION_ID, Constants.API_KEY)

        dummyRetrieval()
    }

    private fun dummyRetrieval() {
        Backendless.Data.of(Sleep::class.java).find(object : AsyncCallback<List<Sleep?>?> {
            override fun handleResponse(foundSleeps: List<Sleep?>?) {
                Log.d(TAG, "foundSleeps: $foundSleeps")
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(TAG, "handleFault: ${fault.message}")
            }
        })
    }

}