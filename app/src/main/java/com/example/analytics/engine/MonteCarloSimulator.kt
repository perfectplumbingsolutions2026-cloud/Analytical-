package com.example.analytics.engine

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

object MonteCarloSimulator {

    data class SimulationResult(
        val expectedLambda: Double,
        val overProbability: Double, // 0.0 to 1.0
        val underProbability: Double, // 0.0 to 1.0
        val simulationVariance: Double,
        val stdDeviation: Double,
        val ci95Lower: Double,
        val ci95Upper: Double,
        val agreementPercent: Int,
        val simulatedDistribution: List<Int> // Distribution for chart visualization (e.g., histogram buckets)
    )

    /**
     * Executes 50,000 Monte Carlo iterations using Poisson distribution & Bayesian updating.
     */
    fun runSimulation(
        baseLambda: Double,
        line: Double,
        numSimulations: Int = 50000,
        varianceNoiseFactor: Double = 0.08
    ): SimulationResult {
        var overCount = 0
        var underCount = 0
        var totalSimulatedValue = 0.0
        var squaredValueSum = 0.0

        val histogramBuckets = IntArray(12) { 0 } // Buckets for visual distribution
        val rng = Random(baseLambda.hashCode() + line.hashCode())

        // Bayesian updated prior mean
        val bayesianPriorLambda = baseLambda * 0.95 + 0.05 * line

        for (i in 0 until numSimulations) {
            // Apply slight match tempo fluctuation per simulated match run
            val tempoVariation = 1.0 + rng.nextGaussian() * varianceNoiseFactor
            val adjustedLambda = (bayesianPriorLambda * tempoVariation).coerceAtLeast(0.1)

            // Sample from Poisson distribution with adjusted Lambda
            val simVal = samplePoisson(adjustedLambda, rng)

            totalSimulatedValue += simVal
            squaredValueSum += (simVal * simVal)

            if (simVal > line) {
                overCount++
            } else {
                underCount++
            }

            // Map into histogram bucket for UI
            val bucketIndex = (simVal.toDouble() / (line * 2.0 / 12.0)).toInt().coerceIn(0, 11)
            histogramBuckets[bucketIndex]++
        }

        val expectedLambda = totalSimulatedValue / numSimulations
        val variance = (squaredValueSum / numSimulations) - (expectedLambda * expectedLambda)
        val stdDev = sqrt(maxOf(0.01, variance))

        val overProb = overCount.toDouble() / numSimulations
        val underProb = underCount.toDouble() / numSimulations

        // 95% Confidence Interval for mean lambda
        val marginOfError = 1.96 * (stdDev / sqrt(numSimulations.toDouble()))
        val ciLower = maxOf(0.0, expectedLambda - marginOfError)
        val ciUpper = expectedLambda + marginOfError

        // Model agreement percent
        val dominantProb = maxOf(overProb, underProb)
        val agreementPercent = (dominantProb * 100).toInt().coerceIn(50, 99)

        return SimulationResult(
            expectedLambda = expectedLambda,
            overProbability = overProb,
            underProbability = underProb,
            simulationVariance = variance,
            stdDeviation = stdDev,
            ci95Lower = ciLower,
            ci95Upper = ciUpper,
            agreementPercent = agreementPercent,
            simulatedDistribution = histogramBuckets.toList()
        )
    }

    /**
     * Poisson random sample generator (Knuth's algorithm / Transformation method)
     */
    private fun samplePoisson(lambda: Double, rng: Random): Int {
        val l = exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= rng.nextDouble()
        } while (p > l && k < 100)
        return k - 1
    }

    private fun Random.nextGaussian(): Double {
        // Box-Muller transform
        val u1 = nextDouble().coerceAtLeast(1e-10)
        val u2 = nextDouble()
        return sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * Math.PI * u2)
    }
}
