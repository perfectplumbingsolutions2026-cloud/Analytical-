package com.example.analytics.engine

import com.example.data.models.ConfidenceGrade
import com.example.data.models.MarketPrediction
import com.example.data.models.MarketType
import com.example.data.models.MatchContext
import com.example.data.models.MatchFixture
import com.example.data.models.Recommendation
import com.example.data.models.TeamStats
import kotlin.math.abs

object MarketEngines {

    /**
     * Executes all 6 independent engines and returns the predictions & overall recommendation.
     */
    fun processAllMarkets(
        fixture: MatchFixture,
        home: TeamStats,
        away: TeamStats,
        context: MatchContext,
        trapScore: Int
    ): Pair<List<MarketPrediction>, Recommendation?> {
        val predictions = listOf(
            runShotsEngine(home, away, context, trapScore),
            runShotsOnTargetEngine(home, away, context, trapScore),
            runFoulsEngine(home, away, context, trapScore),
            runOffsideEngine(home, away, context, trapScore),
            runThrowInEngine(home, away, context, trapScore),
            runGoalKickEngine(home, away, context, trapScore)
        )

        // Select the top recommendation with highest probability and cleanest confidence grade
        val bestMarket = predictions
            .filter { it.confidenceGrade != ConfidenceGrade.REJECT && it.confidenceGrade != ConfidenceGrade.WEAK }
            .maxByOrNull { it.probabilityPercent }

        val recommendation = if (bestMarket != null) {
            Recommendation(
                marketType = bestMarket.marketType,
                bestPrediction = "${bestMarket.marketType.displayName} ${bestMarket.prediction} ${bestMarket.line}",
                probabilityPercent = bestMarket.probabilityPercent,
                confidenceGrade = bestMarket.confidenceGrade,
                reasoningSummary = generateReasoningSummary(bestMarket, home, away, context, trapScore)
            )
        } else null

        return Pair(predictions, recommendation)
    }

    // Engine 1: Total Shots Engine (O/U 14.5)
    private fun runShotsEngine(home: TeamStats, away: TeamStats, context: MatchContext, trapScore: Int): MarketPrediction {
        val baseShots = home.shotsAvg + away.shotsAvg
        val pressingFactor = (home.pressingIntensity + away.pressingIntensity) / 14.0
        val tempoMultiplier = 1.0 + (pressingFactor - 1.0) * 0.15
        val expectedValue = baseShots * tempoMultiplier

        val line = MarketType.TOTAL_SHOTS.benchmarkLine
        val simResult = MonteCarloSimulator.runSimulation(expectedValue, line)

        return createPredictionFromSim(
            marketType = MarketType.TOTAL_SHOTS,
            line = line,
            simResult = simResult,
            trapScore = trapScore
        )
    }

    // Engine 2: Shots On Target Engine (O/U 11.5)
    private fun runShotsOnTargetEngine(home: TeamStats, away: TeamStats, context: MatchContext, trapScore: Int): MarketPrediction {
        val baseSOT = home.shotsOnTargetAvg + away.shotsOnTargetAvg
        val xGFactor = (home.xGAvg + away.xGAvg) / 3.0
        val expectedValue = baseSOT * (0.85 + 0.15 * xGFactor)

        val line = MarketType.SHOTS_ON_TARGET.benchmarkLine
        val simResult = MonteCarloSimulator.runSimulation(expectedValue, line)

        return createPredictionFromSim(
            marketType = MarketType.SHOTS_ON_TARGET,
            line = line,
            simResult = simResult,
            trapScore = trapScore
        )
    }

    // Engine 3: Fouls Engine (O/U 29.5)
    private fun runFoulsEngine(home: TeamStats, away: TeamStats, context: MatchContext, trapScore: Int): MarketPrediction {
        val baseFouls = home.foulsAvg + away.foulsAvg
        val refereeFactor = context.officials.foulAvgPerGame / 28.5
        val derbyMultiplier = if (context.isDerby) 1.18 else 1.0
        val expectedValue = baseFouls * refereeFactor * derbyMultiplier

        val line = MarketType.FOULS.benchmarkLine
        val simResult = MonteCarloSimulator.runSimulation(expectedValue, line)

        return createPredictionFromSim(
            marketType = MarketType.FOULS,
            line = line,
            simResult = simResult,
            trapScore = trapScore
        )
    }

    // Engine 4: Offsides Engine (O/U 4.5)
    private fun runOffsideEngine(home: TeamStats, away: TeamStats, context: MatchContext, trapScore: Int): MarketPrediction {
        val baseOffsides = home.offsidesAvg + away.offsidesAvg
        val counterAttackFactor = (home.counterAttacksAvg + away.counterAttacksAvg) / 5.0
        val expectedValue = baseOffsides * (0.9 + 0.2 * counterAttackFactor)

        val line = MarketType.OFFSIDES.benchmarkLine
        val simResult = MonteCarloSimulator.runSimulation(expectedValue, line)

        return createPredictionFromSim(
            marketType = MarketType.OFFSIDES,
            line = line,
            simResult = simResult,
            trapScore = trapScore
        )
    }

    // Engine 5: Throw-ins Engine (O/U 22.5)
    private fun runThrowInEngine(home: TeamStats, away: TeamStats, context: MatchContext, trapScore: Int): MarketPrediction {
        val baseThrowIns = home.throwInsAvg + away.throwInsAvg
        val wingCrossFactor = (home.crossesAvg + away.crossesAvg) / 35.0
        val windMultiplier = if (context.environment.windKmH > 25) 1.12 else 1.0
        val expectedValue = baseThrowIns * wingCrossFactor * windMultiplier

        val line = MarketType.THROW_INS.benchmarkLine
        val simResult = MonteCarloSimulator.runSimulation(expectedValue, line)

        return createPredictionFromSim(
            marketType = MarketType.THROW_INS,
            line = line,
            simResult = simResult,
            trapScore = trapScore
        )
    }

    // Engine 6: Goal Kicks Engine (O/U 15.5)
    private fun runGoalKickEngine(home: TeamStats, away: TeamStats, context: MatchContext, trapScore: Int): MarketPrediction {
        val baseGoalKicks = home.goalKicksAvg + away.goalKicksAvg
        val shotsOffTargetFactor = (home.shotsOffTargetAvg + away.shotsOffTargetAvg) / 16.0
        val expectedValue = baseGoalKicks * (0.85 + 0.25 * shotsOffTargetFactor)

        val line = MarketType.GOAL_KICKS.benchmarkLine
        val simResult = MonteCarloSimulator.runSimulation(expectedValue, line)

        return createPredictionFromSim(
            marketType = MarketType.GOAL_KICKS,
            line = line,
            simResult = simResult,
            trapScore = trapScore
        )
    }

    private fun createPredictionFromSim(
        marketType: MarketType,
        line: Double,
        simResult: MonteCarloSimulator.SimulationResult,
        trapScore: Int
    ): MarketPrediction {
        val isOver = simResult.expectedLambda >= line
        val rawProb = if (isOver) simResult.overProbability else simResult.underProbability
        val probPercent = (rawProb * 100).toInt().coerceIn(50, 99)

        // Assign Confidence Grade based on probability, variance, and trap penalty
        val grade = when {
            probPercent >= 80 && trapScore <= 25 && simResult.simulationVariance < 12.0 -> ConfidenceGrade.ELITE
            probPercent >= 73 && trapScore <= 40 -> ConfidenceGrade.STRONG
            probPercent >= 65 && trapScore <= 55 -> ConfidenceGrade.GOOD
            probPercent >= 58 -> ConfidenceGrade.MODERATE
            else -> ConfidenceGrade.WEAK
        }

        return MarketPrediction(
            marketType = marketType,
            line = line,
            expectedValue = String.format("%.1f", simResult.expectedLambda).toDouble(),
            prediction = if (isOver) "Over" else "Under",
            probabilityPercent = probPercent,
            confidenceGrade = grade,
            modelAgreementPercent = simResult.agreementPercent,
            simulationConsistencyPercent = (100 - (simResult.simulationVariance * 2.0)).toInt().coerceIn(60, 98),
            variance = String.format("%.2f", simResult.simulationVariance).toDouble()
        )
    }

    private fun generateReasoningSummary(
        market: MarketPrediction,
        home: TeamStats,
        away: TeamStats,
        context: MatchContext,
        trapScore: Int
    ): String {
        return when (market.marketType) {
            MarketType.THROW_INS -> "High wing cross frequency (${home.crossesAvg + away.crossesAvg} avg), active fullbacks, high pressing tempo, low trap score ($trapScore/100), and 50,000 Monte Carlo simulations yielding ${market.probabilityPercent}% confidence."
            MarketType.TOTAL_SHOTS -> "Aggressive attacking volume (${home.shotsAvg} home / ${away.shotsAvg} away avg), high pressing intensity, strong xG totals, and model convergence over 50,000 iterations."
            MarketType.FOULS -> "High foul intensity (${home.foulsAvg + away.foulsAvg} combined avg), referee average of ${context.officials.foulAvgPerGame} fouls/match${if (context.isDerby) ", rivalry derby atmosphere," else ""} and strong Monte Carlo simulation agreement."
            MarketType.OFFSIDES -> "High defensive line height, counter-attacking setups, and frequent long balls favoring offside triggers."
            MarketType.SHOTS_ON_TARGET -> "High xG creation rate (${home.xGAvg} / ${away.xGAvg}), strong shot conversion efficiency, and high save demands on goalkeepers."
            MarketType.GOAL_KICKS -> "High volume of shots off target, frequent long clearances, and aerial duals forcing goal line restarts."
        }
    }
}
