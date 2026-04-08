package com.activitytracker

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ActivityItem(
    val id: String,
    val name: String,
    val count: Int,
    val note: String? = null
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromActivityItemList(value: List<ActivityItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toActivityItemList(value: String): List<ActivityItem> {
        val listType = object : TypeToken<List<ActivityItem>>() {}.type
        return gson.fromJson(value, listType)
    }
}

@Entity(tableName = "daily_data")
data class DailyData(
    @PrimaryKey val date: String,
    val activities: List<ActivityItem>
)
