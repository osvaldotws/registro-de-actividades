package com.activitytracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import java.util.UUID.randomUUID

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewActivities: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnPreviousDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var etNewActivityName: EditText
    private lateinit var btnAddActivity: MaterialButton
    private lateinit var addActivityForm: MaterialCardView
    
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var database: ActivityDatabase
    
    private var selectedDate: String = getCurrentDate()
    private var currentActivities: MutableList<ActivityItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        initViews()
        
        // Initialize database
        database = ActivityDatabase.getDatabase(this)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup click listeners
        setupClickListeners()
        
        // Load data for selected date
        loadDataForDate(selectedDate)
    }
    
    private fun initViews() {
        recyclerViewActivities = findViewById(R.id.recyclerViewActivities)
        emptyState = findViewById(R.id.emptyState)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnPreviousDay = findViewById(R.id.btnPreviousDay)
        btnNextDay = findViewById(R.id.btnNextDay)
        etNewActivityName = findViewById(R.id.etNewActivityName)
        btnAddActivity = findViewById(R.id.btnAddActivity)
        addActivityForm = findViewById(R.id.addActivityForm)
        
        // Set initial date display
        updateDateDisplay()
    }
    
    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(
            activities = currentActivities,
            onCountChanged = { id, delta -> updateCount(id, delta) },
            onNoteChanged = { id, note -> updateNote(id, note) },
            onDeleteClick = { id -> deleteActivity(id) }
        )
        
        recyclerViewActivities.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = activityAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnPreviousDay.setOnClickListener { changeDate(-1) }
        btnNextDay.setOnClickListener { changeDate(1) }
        
        btnAddActivity.setOnClickListener { addActivity() }
        
        // Click on date to show date picker could be added here
        tvSelectedDate.setOnClickListener { /* Could open date picker */ }
    }
    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }
    
    private fun updateDateDisplay() {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "ES"))
        val date = inputFormat.parse(selectedDate)
        val formattedDate = date?.let { outputFormat.format(it) } ?: selectedDate
        tvSelectedDate.text = formattedDate
    }
    
    private fun changeDate(days: Int) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        try {
            val date = inputFormat.parse(selectedDate)
            calendar.time = date!!
            calendar.add(Calendar.DAY_OF_YEAR, days)
            selectedDate = inputFormat.format(calendar.time)
            updateDateDisplay()
            loadDataForDate(selectedDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadDataForDate(date: String) {
        lifecycleScope.launch {
            val dailyData = withContext(Dispatchers.IO) {
                database.activityDao().getDailyData(date)
            }
            
            currentActivities.clear()
            if (dailyData != null) {
                currentActivities.addAll(dailyData.activities)
            }
            
            activityAdapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }
    
    private fun updateEmptyState() {
        if (currentActivities.isEmpty()) {
            recyclerViewActivities.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            recyclerViewActivities.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }
    
    private fun addActivity() {
        val name = etNewActivityName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un nombre de actividad", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newActivity = ActivityItem(
            id = randomUUID().toString(),
            name = name,
            count = 0,
            note = null
        )
        
        currentActivities.add(newActivity)
        activityAdapter.notifyDataSetChanged()
        saveData()
        
        etNewActivityName.text.clear()
        updateEmptyState()
    }
    
    private fun updateCount(id: String, delta: Int) {
        val activity = currentActivities.find { it.id == id } ?: return
        val newIndex = (activity.count + delta).coerceAtLeast(0)
        
        activity.currentActivitiesIndex()?.let { index ->
            currentActivities[index] = activity.copy(count = newIndex)
            activityAdapter.notifyItemChanged(index)
            saveData()
        }
    }
    
    private fun List<ActivityItem>.indexOfActivity(id: String): Int {
        return indexOfFirst { it.id == id }
    }
    
    private fun ActivityItem.currentActivitiesIndex(): Int {
        return currentActivities.indexOfFirst { it.id == this.id }
    }
    
    private fun updateNote(id: String, note: String) {
        val index = currentActivities.indexOfFirst { it.id == id }
        if (index != -1) {
            currentActivities[index] = currentActivities[index].copy(note = note.ifEmpty { null })
            saveData()
        }
    }
    
    private fun deleteActivity(id: String) {
        val index = currentActivities.indexOfFirst { it.id == id }
        if (index != -1) {
            currentActivities.removeAt(index)
            activityAdapter.notifyItemRemoved(index)
            saveData()
            updateEmptyState()
        }
    }
    
    private fun saveData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dailyData = DailyData(
                    date = selectedDate,
                    activities = currentActivities
                )
                database.activityDao().insertDailyData(dailyData)
            }
        }
    }
}
