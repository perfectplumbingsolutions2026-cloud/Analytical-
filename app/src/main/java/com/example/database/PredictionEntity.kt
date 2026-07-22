package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions_history")
data class PredictionRecordEntity(
    @PrimaryKey val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val competitionType: String,
    val matchTime: String,
    val isQualified: Boolean,
    val rejectionReasons: String, // Comma separated or empty
    val trapScore: Int,
    val dataReliabilityScore: Int,
    val topRecommendedMarket: String,
    val topProbabilityPercent: Int,
    val topConfidenceGrade: String,
    val topReasoning: String,
    val predictionsSummaryJson: String, // Summary of 6 markets
    val timestamp: Long = System.currentTimeMillis(),
    val actualOutcome: String? = null, // e.g. "WIN" or "LOSS" or "PENDING"
    val actualValueRecorded: Double? = null
)
