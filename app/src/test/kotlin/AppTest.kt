/*
 * This source file was generated by the Gradle 'init' task
 */
package FinTool

import kotlinx.coroutines.*
import java.io.IOException

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull


class AppTest {
    // Staging input data for testing
    private val jsonText1 = """ {
    "Meta Data": {
        "1. Information": "Daily Prices (open, high, low, close) and Volumes",
        "2. Symbol": "GOOGL",
        "3. Last Refreshed": "2024-12-04",
        "4. Output Size": "Compact",
        "5. Time Zone": "US/Eastern"
    },
    "Time Series (Daily)": {
        "2024-12-04": {
            "1. open": "171.1450",
            "2. high": "174.9100",
            "3. low": "171.0600",
            "4. close": "174.3700",
            "5. volume": "31615137"
        },
        "2024-12-03": {
            "1. open": "171.4900",
            "2. high": "172.6800",
            "3. low": "170.8500",
            "4. close": "171.3400",
            "5. volume": "22248705"
        }
    }}
    """

    private fun areDoublesEqual(a: Double, b: Double, epsilon: Double = 1e-9): Boolean {
      return Math.abs(a - b) < epsilon
    }
      
    @Test fun appCanParseAVJsonFormat() = runBlocking {
      val classUnderTest = ERManager(jsonText1, mocking = true)

      val apiKey = ConfigManager().getAPIKey()
      val symbol = "GOOGL"
      val interval = "5min"
      val fequency = "TIME_SERIES_DAILY"

      val prices = classUnderTest.fetchData(symbol, interval, fequency, apiKey)
      assertNotNull(prices)
      assertTrue(areDoublesEqual(prices["2024-12-04"]?.close?: 0.0, 174.37))
      assertTrue(areDoublesEqual(prices["2024-12-03"]?.close?: 0.0, 171.34))
    }
}
