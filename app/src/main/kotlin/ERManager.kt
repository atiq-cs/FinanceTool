/**
 * Finance Tool
 *  Supports Assets Earnings release related Ops
 */
package FinTool

import kotlinx.coroutines.*
import java.io.IOException

// ktor related packages
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.call.body

// Serialization and json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


@Serializable
data class PricesDaily(
  @SerialName("4. close")   val close: Double = 0.0,
  @SerialName("1. open")    val open: Double = 0.0,
  @SerialName("3. low")     val low: Double = 0.0,
  @SerialName("2. high")    val high: Double = 0.0,
  @SerialName("5. volume")  val volume: Long = 0L,
)

@Serializable
data class TimeSeriesDaily(
  // @SerialName("Meta Data")            val metaData: MetaDataInfo,
  @SerialName("Time Series (Daily)") val prices: Map<String, PricesDaily>,
)

// @Serializable
// data class MetaDataInfo(
//   @SerialName("1. Information")     val info: String,
//   @SerialName("2. Symbol")          val sym: String,
//   @SerialName("3. Last Refreshed")  val refresh: String,
//   @SerialName("4. Output Size")     val high: String,
//   @SerialName("5. Time Zone")       val tz: String,
// )



class ERManager (
  // Testing variables
  val mJsonText: String = "",     // for mocking / testing
  val mocking: Boolean = false) {

  // secondary constructor to initialize private properties
  // constructor(
  // ) {
  // }

  /**
   * Run the program with async / kotlin coroutine support
   */
  fun runAsync() = runBlocking {
    val apiKey = ConfigManager().getAPIKey()
    val symbol = "GOOGL"
    val interval = "5min"
    val fequency = "TIME_SERIES_DAILY"

    val tsd = fetchData(symbol, interval, fequency, apiKey)
    if (tsd != null) {
      // Iterate using forEach
      tsd.prices.forEach { (key, value) ->
        println("Date: $key, closing price: ${value.close}")
      }
    }
  }

  suspend fun fetchData(
    symbol: String,
    interval: String,
    frequency: String,
    apiKey: String,
  ): TimeSeriesDaily? {

    val client = HttpClient(CIO) {
      install(ContentNegotiation) {
          json(Json { ignoreUnknownKeys = true }) // Safely handles unknown fields
      }
    }
    val customJson = Json { ignoreUnknownKeys = true }

    val finDataProviderURL = "https://www.alphavantage.co/query"
    val url = finDataProviderURL + "?function=" + frequency + "&symbol=" +
      symbol + "&interval=" + interval + "&apikey=" + apiKey


    if (mocking) {
      // Deserialize JSON into the User class
      val tsd = customJson.decodeFromString<TimeSeriesDaily>(mJsonText)
      return tsd
    }

    return try {
      val jsonText = client.get(url).bodyAsText()

      // Deserialize JSON into the User class
      val tsd = customJson.decodeFromString<TimeSeriesDaily>(jsonText)
      tsd
    } catch (e: Exception) {
        println("Error: ${e.message}")
        null
    } finally {
        client.close()
    }
  }

  // Test Helpers
  // fun getMessage(): String {
  //   return message
  // }
}
