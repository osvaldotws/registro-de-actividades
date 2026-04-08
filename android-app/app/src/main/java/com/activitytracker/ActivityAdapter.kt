package com.activitytracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.activitytracker.ActivityItem
import com.google.android.material.card.MaterialCardView

class ActivityAdapter(
    private val activities: MutableList<ActivityItem>,
    private val onCountChanged: (String, Int) -> Unit,
    private val onNoteChanged: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvActivityName: TextView = itemView.findViewById(R.id.tvActivityName)
        val etActivityNote: EditText = itemView.findViewById(R.id.etActivityNote)
        val tvCount: TextView = itemView.findViewById(R.id.tvCount)
        val btnDecrease: ImageButton = itemView.findViewById(R.id.btnDecrease)
        val btnIncrease: ImageButton = itemView.findViewById(R.id.btnIncrease)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.tvActivityName.text = activity.name
        holder.etActivityNote.setText(activity.note ?: "")
        holder.tvCount.text = activity.count.toString()
        
        // Handle note changes
        holder.etActivityNote.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newNote = holder.etActivityNote.text.toString()
                if (newNote != activity.note) {
                    onNoteChanged(activity.id, newNote)
                }
            }
        }
        
        // Handle count decrease
        holder.btnDecrease.setOnClickListener {
            if (activity.count > 0) {
                onCountChanged(activity.id, -1)
            }
        }
        
        // Handle count increase
        holder.btnIncrease.setOnClickListener {
            onCountChanged(activity.id, 1)
        }
        
        // Handle delete
        holder.btnDelete.setOnClickListener {
            onDeleteClick(activity.id)
        }
    }

    override fun getItemCount() = activities.size
}
