package com.mistershorr.loginandregistration

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.mistershorr.loginandregistration.sleeping.Sleep
import com.mistershorr.loginandregistration.sleeping.SleepDetailActivity
import com.mistershorr.loginandregistration.sleeping.SleepListActivity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class SleepAdapter (var sleepList: MutableList<Sleep>): RecyclerView.Adapter<SleepAdapter.ViewHolder>() {

    companion object {
        const val TAG = "SleepAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDate : TextView
        val textViewHours: TextView
        val textViewDuration: TextView
        val layout : ConstraintLayout
        val ratingBarQuality : RatingBar
        init {
            textViewDate = view.findViewById(R.id.textView_itemSleep_date)
            textViewDuration = view.findViewById(R.id.textView_itemSleep_duration)
            textViewHours = view.findViewById(R.id.textView_itemSleep_hours)
            layout = view.findViewById(R.id.layout_itemSleep)
            ratingBarQuality = view.findViewById(R.id.ratingBar_itemSleep_sleepQuality)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sleep = sleepList[position]
        val context = holder.layout.context

        val formatter = DateTimeFormatter.ofPattern("yyy-MM-dd")
        val sleepDate = LocalDateTime.ofEpochSecond(sleep.sleepDateMillis / 1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        holder.textViewDate.text = formatter.format(sleepDate)

        // calculate the difference in time from bed to wake and convert to hours & minutes
        // use String.format() to display it in HH:mm format in the duration textview
        // hint: you need leading zeroes and a width of 2
        val difference = sleep.wakeMillis - sleep.bedMillis
        val sleepTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(difference),
            TimeUnit.MILLISECONDS.toMinutes(difference) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(difference)),
            TimeUnit.MILLISECONDS.toSeconds(difference) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(difference)))
        holder.textViewDuration.text = sleepTime
        Log.d(TAG, "sleepTime: $sleepTime")


        // sets the actual hours slept textview
        val bedTime = LocalDateTime.ofEpochSecond(sleep.bedMillis / 1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val wakeTime = LocalDateTime.ofEpochSecond(sleep.wakeMillis / 1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        holder.textViewHours.text = "${timeFormatter.format(bedTime)} - ${timeFormatter.format(wakeTime)}"

        holder.ratingBarQuality.rating = sleep.quality.toFloat()


        holder.layout.setOnClickListener {
            val intent = Intent(context, SleepDetailActivity::class.java).apply {
                putExtra(SleepDetailActivity.EXTRA_SLEEP, sleep)
            }
            context.startActivity(intent)
        }

        holder.layout.isLongClickable = true
        holder.layout.setOnLongClickListener {
            val popMenu = PopupMenu(context, holder.layout)
            popMenu.inflate(R.menu.menu_sleep_list_context)
            popMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_sleeplist_delete -> {
                        deleteFromBackendless(position)
                        notifyDataSetChanged()
                        true
                    }
                    else -> true
                }
            }
            popMenu.show()
            true
        }
    }

    private fun deleteFromBackendless(position: Int) {
        Log.d("SleepAdapter", "deleteFromBackendless: Trying to delete ${sleepList[position]}")
        // put in the code to delete the item using the callback from Backendless
        // in the handleResponse, we'll need to also delete the item from the sleepList
        // and make sure that the recyclerview is updated

        val sleepRecord = sleepList[position]
        sleepRecord.ownerId = Backendless.UserService.CurrentUser().userId

        Backendless.Data.of(Sleep::class.java).save(sleepRecord, object : AsyncCallback<Sleep?> {
            override fun handleResponse(sleepRecord: Sleep?) {
                Backendless.Data.of(Sleep::class.java).remove(sleepRecord,
                    object : AsyncCallback<Long?> {
                        override fun handleResponse(response: Long?) {
                            Log.d(SleepListActivity.TAG, "Object Deleted")
                            sleepList.removeAt(position)
                            notifyDataSetChanged()
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            Log.d(SleepListActivity.TAG, fault.message)
                        }
                    }
                )
                notifyDataSetChanged()
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.d(SleepListActivity.TAG, fault.message)
            }
        })
    }

    override fun getItemCount() = sleepList.size
}