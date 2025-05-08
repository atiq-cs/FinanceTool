/**
 * Finance Tool
 *  Supports Assets Earnings release related Ops
 *  Given assets table in map format, find delta etc.
 * 
 * Available Ops
 * - get output table based on input table (extract delta for specific days)
 * - compute premium
 */
package FinTool

import kotlinx.coroutines.*
import java.io.IOException


class ERManager {
  // secondary constructor to initialize private properties
  // constructor(
  // ) {
  // }

  /**
   * Run the program with async / kotlin coroutine support
   */
  fun runAsync(assetsMap: MutableMap<String, MutableList<String>>) = runBlocking {
    val dataProvider = UnifiedDataProvider()

    // keep this block
    assetsMap.forEach { (key, value) ->
      println(key.substringBeforeLast('-'))

      value.forEach { symbol ->
        // Closing price pair
        val cPair = dataProvider.getClosingPricePair(key, symbol)
        val delta = String.format("%.2f", 100.0 * cPair.current / cPair.prior - 100.0).toFloat()
        // For debugging: add " | $cPair" to the statement
        println(" $symbol | " + ( if (delta>0.0) " +" else " " ) + "$delta% ${cPair.current}")
      }
    }

  }
}
