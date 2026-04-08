package com.activitytracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.activitytracker.Converters
import com.activitytracker.DailyData
import com.activitytracker.dao.ActivityDao

@Database(entities = [DailyData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ActivityDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: ActivityDatabase? = null

        fun getDatabase(context: Context): ActivityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ActivityDatabase::class.java,
                    "activity_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
