package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {

    @Query("SELECT * FROM predictions_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<PredictionRecordEntity>>

    @Query("SELECT * FROM predictions_history WHERE isQualified = 1 ORDER BY timestamp DESC")
    fun getQualifiedRecords(): Flow<List<PredictionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PredictionRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PredictionRecordEntity>)

    @Query("UPDATE predictions_history SET actualOutcome = :outcome, actualValueRecorded = :value WHERE id = :id")
    suspend fun updateActualResult(id: String, outcome: String, value: Double)

    @Query("DELETE FROM predictions_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM predictions_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM predictions_history WHERE actualOutcome IS NOT NULL AND actualOutcome != 'PENDING'")
    fun getVerifiedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM predictions_history WHERE actualOutcome = 'WIN'")
    fun getWinningCount(): Flow<Int>
}
