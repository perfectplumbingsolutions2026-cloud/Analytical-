package com.example.analytics.engine

import com.example.data.models.MatchFixture
import com.example.data.models.QualificationResult
import com.example.data.models.TeamStats

object QualificationEngine {

    /**
     * Validates fixture readiness before prediction runs.
     * Checks:
     * - Minimum attacking metrics
     * - Minimum possession
     * - Minimum shots
     * - Minimum data reliability
     * Returns AUTO REJECT with reasons if failed.
     */
    fun qualifyFixture(
        fixture: MatchFixture,
        homeStats: TeamStats,
        awayStats: TeamStats,
        dataReliabilityScore: Int
    ): QualificationResult {
        val rejectionReasons = mutableListOf<String>()

        // Rule 1: Data Reliability threshold
        if (dataReliabilityScore < 70) {
            rejectionReasons.add("Insufficient data reliability: Score $dataReliabilityScore% is below minimum 70%")
        }

        // Rule 2: Combined shots threshold
        val totalShots = homeStats.shotsAvg + awayStats.shotsAvg
        if (totalShots < 14.0) {
            rejectionReasons.add("Insufficient total shot volume: Combined average $totalShots is below minimum 14.0 shots")
        }

        // Rule 3: Minimum attacking metrics (xG + xGA)
        val homeXG = homeStats.xGAvg
        val awayXG = awayStats.xGAvg
        if (homeXG < 0.5 && awayXG < 0.5) {
            rejectionReasons.add("Critical lack of attacking metrics: Both teams xG average below 0.5")
        }

        // Rule 4: Severe missing / distorted possession
        if (homeStats.possessionAvg < 20.0 || awayStats.possessionAvg < 20.0) {
            rejectionReasons.add("Unusable possession profile: Extreme outlier possession (< 20%)")
        }

        val isQualified = rejectionReasons.isEmpty()
        return QualificationResult(
            isQualified = isQualified,
            rejectionReasons = rejectionReasons
        )
    }
}
