package com.activitytracker.dao

import androidx.room.*
import com.activitytracker.DailyData
import com.activitytracker.ActivityItem

@Dao
interface ActivityDao {
    @Query("SELECT * FROM daily_data WHERE date = :date")
    suspend fun getDailyData(date: String): DailyData?

    @Query("SELECT * FROM daily_data ORDER BY date DESC")
    suspend fun getAllDailyData(): List<DailyData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyData(dailyData: DailyData)

    @Delete
    suspend fun deleteDailyData(dailyData: DailyData)

    @Query("DELETE FROM daily_data WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
