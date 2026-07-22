package com.example.analytics.engine

import com.example.data.models.CompetitionType
import com.example.data.models.MatchContext
import com.example.data.models.MatchFixture
import com.example.data.models.TeamStats
import com.example.data.models.TrapDetection

object TrapDetector {

    fun analyzeTrap(
        fixture: MatchFixture,
        homeStats: TeamStats,
        awayStats: TeamStats,
        context: MatchContext,
        reliabilityScore: Int
    ): TrapDetection {
        var score = 0
        val factors = mutableListOf<String>()

        // 1. Friendly or Reserve Competition Risk
        if (fixture.competitionType == CompetitionType.FRIENDLY) {
            score += 35
            factors.add("Friendly match: Low competitive intensity & high substitution frequency")
        } else if (fixture.competitionType == CompetitionType.RESERVE || fixture.competitionType == CompetitionType.YOUTH) {
            score += 25
            factors.add("Youth/Reserve fixture: High tactical volatility & team lineup rotation")
        }

        // 2. High Rotation Risk
        if (context.rotationRiskPercent > 30) {
            score += 20
            factors.add("Rotation risk (${context.rotationRiskPercent}%): Key squad players likely rested")
        }

        // 3. Defensive Low-Attacking Intent
        val combinedShots = homeStats.shotsAvg + awayStats.shotsAvg
        if (combinedShots < 18.0) {
            score += 18
            factors.add("Low attacking volume: Combined average shots below 18.0 ($combinedShots)")
        }

        // 4. Extreme Possession Imbalance / Low Attacking Entries
        if (homeStats.possessionAvg < 35.0 || awayStats.possessionAvg < 35.0) {
            score += 15
            factors.add("Severe possession imbalance (Low possession side < 35%)")
        }

        // 5. Environmental / Severe Weather
        if (context.environment.rainProbabilityPercent > 70 || context.environment.windKmH > 35) {
            score += 12
            factors.add("Adverse weather: Rain ${context.environment.rainProbabilityPercent}% / Wind ${context.environment.windKmH}km/h disrupting ball mechanics")
        }

        // 6. Low Data Reliability / Source Disagreement
        if (reliabilityScore < 80) {
            score += 20
            factors.add("Source data disagreement: Reliability score below 80% ($reliabilityScore%)")
        }

        // 7. Relegation / Low Motivation Match
        if (context.matchImportance <= 4) {
            score += 10
            factors.add("Low fixture stakes: Match importance rating $context.matchImportance/10")
        }

        val finalScore = score.coerceIn(0, 100)
        return TrapDetection(
            trapScore = finalScore,
            riskFactors = if (factors.isEmpty()) listOf("Low statistical risk detected. Clean fixture parameters.") else factors
        )
    }
}
