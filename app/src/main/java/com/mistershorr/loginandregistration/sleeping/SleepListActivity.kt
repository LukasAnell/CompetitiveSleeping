package com.mistershorr.loginandregistration.sleeping

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.mistershorr.loginandregistration.LoginActivity
import com.mistershorr.loginandregistration.databinding.ActivitySleepListBinding
import java.util.Date


class SleepListActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SleepListActivity"
    }

    private lateinit var binding: ActivitySleepListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadDataFromBackendless()


    }

    private fun deleteSleepRecord() {
        val sleepRecord = Sleep(
            Date(),
            Date(),
            Date(),
            9,
            "very nice sleep and good dreams too"
        )
        sleepRecord.ownerId = Backendless.UserService.CurrentUser().userId

        Backendless.Data.of(Sleep::class.java).save(sleepRecord, object : AsyncCallback<Sleep?> {
            override fun handleResponse(sleepRecord: Sleep?) {
                Backendless.Data.of(Sleep::class.java).remove(sleepRecord,
                    object : AsyncCallback<Long?> {
                        override fun handleResponse(response: Long?) {
                            // Contact has been deleted. The response is the
                            // time in milliseconds when the object was deleted
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            // an error has occurred, the error code can be
                            // retrieved with fault.getCode()
                        }
                    })
            }

            override fun handleFault(fault: BackendlessFault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
            }
        })
    }

    private fun updateSleepRecord() {
        val sleepRecord = Sleep(
            Date(),
            Date(),
            Date(),
            9,
            "very nice sleep and good dreams too"
        )
        sleepRecord.ownerId = Backendless.UserService.CurrentUser().userId

        Backendless.Data.of(Sleep::class.java).save(sleepRecord, object : AsyncCallback<Sleep> {
            override fun handleResponse(sleepRecord: Sleep) {
                // set new fields based on user input
                // sleepRecord.sleepDate = binding.dateInput.date
                Backendless.Data.of(Sleep::class.java)
                    .save(sleepRecord, object : AsyncCallback<Sleep?> {
                        override fun handleResponse(response: Sleep?) {
                            // Contact instance has been updated
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            Log.d(TAG, "handleFault: ${fault.message}")
                        }
                    })
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(TAG, "handleFault: ${fault.message}")
            }
        })
    }

    private fun addSleepRecord() {
        val sleepRecord = Sleep(
            Date(),
            Date(),
            Date(),
            9,
            "very nice sleep and good dreams too"
        )
        sleepRecord.ownerId = Backendless.UserService.CurrentUser().userId


        // save object asynchronously
        Backendless.Data.of(Sleep::class.java).save(sleepRecord, object : AsyncCallback<Sleep?> {
            override fun handleResponse(response: Sleep?) {
                Log.d(TAG, "response: $response")
            }

            override fun handleFault(fault: BackendlessFault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
                Log.d(TAG, "handleFault: ${fault.message}")
            }
        })
    }

    private fun loadDataFromBackendless() {
        val userId = Backendless.UserService.CurrentUser().userId
        val whereClause = "ownerId = '$userId'"
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = whereClause
        Backendless.Data.of(Sleep::class.java).find(queryBuilder, object: AsyncCallback<List<Sleep?>> {
            override fun handleResponse(sleepList: List<Sleep?>?) {
                Log.d(LoginActivity.TAG, "handleResponse: $sleepList")
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(LoginActivity.TAG, "handleFault: ${fault.message}")
            }
        })
    }

}