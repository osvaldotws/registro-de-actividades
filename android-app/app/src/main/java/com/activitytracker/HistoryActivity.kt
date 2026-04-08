package com.activitytracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.activitytracker.database.ActivityDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var emptyStateHistory: LinearLayout
    private lateinit var toolbarHistory: View
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var database: ActivityDatabase
    
    private var allDailyData: List<DailyData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        // Initialize views
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        emptyStateHistory = findViewById(R.id.emptyStateHistory)
        toolbarHistory = findViewById(R.id.toolbarHistory)
        
        // Setup toolbar navigation
        toolbarHistory.findViewById<View>(R.id.toolbarHistory).setOnClickListener {
            finish()
        }
        
        // Initialize database
        database = ActivityDatabase.getDatabase(this)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Load history data
        loadHistoryData()
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { date ->
            // Navigate back to main activity with selected date
            val resultIntent = Intent().apply {
                putExtra("SELECTED_DATE", date)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        
        recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }
    
    private fun loadHistoryData() {
        lifecycleScope.launch {
            val dailyData = withContext(Dispatchers.IO) {
                database.activityDao().getAllDailyData()
                    .filter { it.activities.isNotEmpty() }
            }
            
            allDailyData = dailyData
            
            if (dailyData.isEmpty()) {
                recyclerViewHistory.visibility = View.GONE
                emptyStateHistory.visibility = View.VISIBLE
            } else {
                recyclerViewHistory.visibility = View.VISIBLE
                emptyStateHistory.visibility = View.GONE
                historyAdapter = HistoryAdapter(dailyData) { date ->
                    val resultIntent = Intent().apply {
                        putExtra("SELECTED_DATE", date)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                recyclerViewHistory.adapter = historyAdapter
            }
        }
    }
}
