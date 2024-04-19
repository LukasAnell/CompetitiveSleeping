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

    override fun onResume() {
        super.onResume()
        loadDataFromBackendless()
    }

    private fun refreshList() {
        val serverListAdapter = SleepAdapter(sleepList)

        val recyclerView: RecyclerView = binding.recyclerViewSleepList
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = serverListAdapter
        serverListAdapter.notifyDataSetChanged()
    }

    private fun loadDataFromBackendless() {
        val userId = Backendless.UserService.CurrentUser().userId!!
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