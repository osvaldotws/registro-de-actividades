package com.activitytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.activitytracker.DailyData
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val dailyDataList: List<DailyData>,
    private val onViewDayClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHistoryDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        val btnViewDay: MaterialButton = itemView.findViewById(R.id.btnViewDay)
        val activitiesContainer: LinearLayout = itemView.findViewById(R.id.activitiesContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_date, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = dailyDataList[position]
        
        // Format date in Spanish
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES"))
        val date = inputFormat.parse(data.date)
        val formattedDate = date?.let { 
            outputFormat.format(it).replaceFirstChar { char -> 
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() 
            } 
        } ?: data.date
        
        holder.tvHistoryDate.text = formattedDate
        
        // Populate activities
        holder.activitiesContainer.removeAllViews()
        data.activities.forEach { activity ->
            val textView = TextView(holder.itemView.context).apply {
                text = "${activity.name} - ${activity.count}"
                setPadding(16, 8, 16, 8)
                textSize = 14f
            }
            holder.activitiesContainer.addView(textView)
        }
        
        holder.btnViewDay.setOnClickListener {
            onViewDayClick(data.date)
        }
    }

    override fun getItemCount() = dailyDataList.size
}
