package com.mistershorr.loginandregistration.sleeping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.mistershorr.loginandregistration.LoginActivity
import com.mistershorr.loginandregistration.SleepAdapter
import com.mistershorr.loginandregistration.databinding.ActivitySleepListBinding


class SleepListActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SleepListActivity"
    }

    private lateinit var binding: ActivitySleepListBinding
    private lateinit var sleepList: MutableList<Sleep>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadDataFromBackendless()

        binding.floatingActionButtonAddSleepSleepList.setOnClickListener {
            val intent = Intent(this, SleepDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun refreshList() {
        val serverListAdapter = SleepAdapter(sleepList)

        val recyclerView: RecyclerView = binding.recyclerViewSleepList
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = serverListAdapter
        serverListAdapter.notifyDataSetChanged()
    }

    private fun updateSleepRecord() {
        val sleepRecord = Sleep(
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            5,
            "very okay",
            null,
            null
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
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            5,
            "very okay",
            null,
            null
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
        Backendless.Data.of(Sleep::class.java).find(queryBuilder, object: AsyncCallback<MutableList<Sleep>> {
            override fun handleResponse(list: MutableList<Sleep>?) {
                Log.d(LoginActivity.TAG, "handleResponse: $list")
                sleepList = list!!
                refreshList()
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(LoginActivity.TAG, "handleFault: ${fault.message}")
            }
        })
    }

}