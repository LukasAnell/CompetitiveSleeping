package com.mistershorr.loginandregistration.sleeping

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SleepAdapter(private var sleepList: List<Sleep>): RecyclerView.Adapter<SleepAdapter.ViewHolder>() {
    companion object {
        const val TAG = "SleepAdapter"
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        init {

        }
    }


}