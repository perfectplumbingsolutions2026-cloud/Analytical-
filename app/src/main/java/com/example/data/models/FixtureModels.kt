package com.example.data.models

import androidx.compose.ui.graphics.Color

enum class CompetitionType(val displayName: String) {
    LEAGUE("League"),
    CUP("Domestic Cup"),
    CONTINENTAL("Continental Cup"),
    YOUTH("Youth"),
    WOMEN("Women's"),
    RESERVE("Reserve"),
    FRIENDLY("Friendly")
}

data class MatchFixture(
    val id: String,
    val timeStr: String,
    val homeTeam: String,
    val awayTeam: String,
    val competition: String,
    val country: String,
    val league: String,
    val competitionType: CompetitionType,
    val stadium: String = "Main Arena",
    val isRivalry: Boolean = false
)

data class TeamStats(
    val teamName: String,
    val goalsAvg: Double,
    val xGAvg: Double,
    val xGAAvg: Double,
    val possessionAvg: Double, // 0-100%
    val shotsAvg: Double,
    val shotsOnTargetAvg: Double,
    val shotsOffTargetAvg: Double,
    val cornersAvg: Double,
    val crossesAvg: Double,
    val throwInsAvg: Double,
    val goalKicksAvg: Double,
    val foulsAvg: Double,
    val offsidesAvg: Double,
    val cardsAvg: Double,
    val ppda: Double, // Passes allowed per defensive action
    val defensiveActions: Double,
    val recoveries: Double,
    val pressingIntensity: Double, // 1-10
    val longBallsAvg: Double,
    val progressivePasses: Double,
    val touchesInBox: Double,
    val counterAttacksAvg: Double,
    val buildUpStyle: String, // "Possession", "Direct", "High-Press", "Counter"
    val setPieceRating: Double // 1-10
)

data class EnvironmentalData(
    val weatherCondition: String = "Clear",
    val temperatureCelsius: Int = 18,
    val rainProbabilityPercent: Int = 10,
    val windKmH: Int = 12,
    val pitchCondition: String = "Excellent",
    val altitudeMeters: Int = 120
)

data class OfficialsData(
    val refereeName: String = "Official Referee",
    val foulAvgPerGame: Double = 28.5,
    val cardAvgPerGame: Double = 4.2,
    val advantageStrictness: Double = 6.5
)

data class MatchContext(
    val homeFormLast5: List<String> = listOf("W", "D", "W", "W", "L"),
    val awayFormLast5: List<String> = listOf("L", "W", "D", "L", "W"),
    val homeTablePos: Int = 4,
    val awayTablePos: Int = 9,
    val matchImportance: Int = 8, // 1-10
    val isDerby: Boolean = false,
    val rotationRiskPercent: Int = 15,
    val environment: EnvironmentalData = EnvironmentalData(),
    val officials: OfficialsData = OfficialsData()
)

enum class MarketType(
    val key: String,
    val displayName: String,
    val benchmarkLine: Double,
    val unit: String
) {
    TOTAL_SHOTS("total_shots", "Total Shots", 14.5, "Shots"),
    SHOTS_ON_TARGET("shots_on_target", "Shots on Target", 11.5, "Shots ON"),
    FOULS("fouls", "Total Fouls", 29.5, "Fouls"),
    OFFSIDES("offsides", "Total Offsides", 4.5, "Offsides"),
    THROW_INS("throw_ins", "Total Throw-ins", 22.5, "Throw-ins"),
    GOAL_KICKS("goal_kicks", "Total Goal Kicks", 15.5, "Goal Kicks")
}

enum class ConfidenceGrade(val label: String, val badgeColor: Long) {
    ELITE("ELITE", 0xFF10B981),      // Emerald Green
    STRONG("STRONG", 0xFF059669),    // Dark Emerald
    GOOD("GOOD", 0xFF3B82F6),        // Blue
    MODERATE("MODERATE", 0xFFF59E0B),// Amber
    WEAK("WEAK", 0xFFEF4444),        // Red
    REJECT("AUTO REJECT", 0xFF6B7280) // Gray
}

data class MarketPrediction(
    val marketType: MarketType,
    val line: Double,
    val expectedValue: Double,
    val prediction: String, // "Over" or "Under"
    val probabilityPercent: Int, // 0-100
    val confidenceGrade: ConfidenceGrade,
    val modelAgreementPercent: Int,
    val simulationConsistencyPercent: Int,
    val variance: Double
)

data class TrapDetection(
    val trapScore: Int, // 0-100 (higher = more dangerous trap)
    val riskFactors: List<String>,
    val isHighTrap: Boolean = trapScore >= 50
)

data class QualificationResult(
    val isQualified: Boolean,
    val rejectionReasons: List<String> = emptyList()
)

data class Recommendation(
    val marketType: MarketType,
    val bestPrediction: String, // "Over 22.5"
    val probabilityPercent: Int,
    val confidenceGrade: ConfidenceGrade,
    val reasoningSummary: String
)

data class MatchAnalysisResult(
    val id: String,
    val fixture: MatchFixture,
    val homeStats: TeamStats,
    val awayStats: TeamStats,
    val context: MatchContext,
    val dataReliabilityScore: Int, // 0-100%
    val simulationRuns: Int = 50000,
    val trapDetection: TrapDetection,
    val qualification: QualificationResult,
    val predictions: List<MarketPrediction>,
    val finalRecommendation: Recommendation?,
    val dataSourcesCount: Int = 22,
    val timestamp: Long = System.currentTimeMillis(),
    val actualResults: Map<String, Double>? = null // Store actual stats when available
)

data class ProcessingLogItem(
    val id: String,
    val matchName: String,
    val stage: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)
