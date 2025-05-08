/**
 * Market data API: Alpha Vantage related implementations
 *  AV related nasty details are allowed here
 * Ref Documentation: https://www.alphavantage.co/documentation/#daily
 */

 package FinTool

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
// Serialization Exceptions
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.ExperimentalSerializationApi

// Date time libraries
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.LocalDate
import java.security.InvalidAlgorithmParameterException
import kotlin.jvm.internal.iterator


@Serializable
data class PricesDaily(
  @SerialName("4. close")   val close: Double = 0.0,
  @SerialName("1. open")    val open: Double = 0.0,
  @SerialName("3. low")     val low: Double = 0.0,
  @SerialName("2. high")    val high: Double = 0.0,
  @SerialName("5. volume")  val volume: Long = 0L,
)

@Serializable
data class TimeSeriesID(
  @SerialName("Time Series (60min)") val prices: Map<String, PricesDaily>,
)

@Serializable
data class MetaDataInfo(
  @SerialName("1. Information")     val info: String,
  @SerialName("2. Symbol")          val symbol: String,
  @SerialName("3. Last Refreshed")  val refresh: String,
  @SerialName("4. Output Size")     val high: String,
  @SerialName("5. Time Zone")       val timezone: String,
)

@Serializable
data class TimeSeriesDaily(
  @SerialName("Meta Data")           val metaData: MetaDataInfo,
  @SerialName("Time Series (Daily)") val prices: Map<String, PricesDaily>,
)



class AlphaVantage(
  // for mocking / testing
  val mJsonText: String = "",
  val mocking: Boolean = false  ,
) {
  // Variables for mocking
  /* private val mJsonText: String = """
  { date in AV_TSD_05-07.txt }
  """
  private val mocking = true */

  suspend fun getClosingPricePair (
    date: String,
    symbol: String,
  ): ClosingPair {
    val prices = fetchData(date, symbol)

    val AVPriorDate = convertToAVDate(date)
    //val priorPrice = prices.get(AVPriorDate)?.close?: 0.0
    var pd = prices.get(AVPriorDate)
    if (pd == null)
      throw NullPointerException("Value for key: $AVPriorDate, PricesDaily object is null!")
    val priorPrice = pd.close

    // Find price of next day of ER
    var AVCurrentDate = getNextDay(date)
    if (prices.containsKey(AVCurrentDate) == false) {
      // Fallback to slower method to find next day when that one is market
      //  holiday / not existing in historical data from the data provider
      val keyIterator = prices.keys.iterator()
      var prev = ""
      while (keyIterator.hasNext()) {
        val key = keyIterator.next()

        if (key == AVPriorDate) {
          if (keyIterator.hasNext() == false)
            throw IllegalStateException("Next day of ER is not present in historical data!")

          AVCurrentDate = prev
          println("Next day is $AVCurrentDate")

          if (AVCurrentDate.isEmpty())
            throw IllegalStateException("Next day of ER is not present in historical data even though ER day is present!")
          break
        } 
        prev = key
      }
    }

    if (prices.containsKey(AVCurrentDate) == false) {
      throw IllegalArgumentException("Data is probably out of range, date $AVCurrentDate is not found!")
    }

    pd = prices.get(AVCurrentDate)
    if (pd == null)
      throw NullPointerException("Value for key: $AVCurrentDate, PricesDaily object is null!")
    val currentPrice = pd.close

    return ClosingPair(currentPrice, priorPrice)
  }

  suspend fun convertToAVDate(
    date: String
  ) : String
  {
    if (date.isEmpty() || date.isBlank())
      throw IllegalArgumentException("\nDate '$date', blank string? ${date.isBlank()}, empty string? ${date.isEmpty()}\n")

    val tokens = date.split("-")
    if (tokens.size < 3)
      throw IllegalArgumentException("\nNumber of tokens less than 3; date: '$date'")

    return (tokens[2] + '-' + tokens[0] + '-' + tokens[1])
  }

  suspend fun getNextDay(
    dateStr: String
  ) : String
  {
    var date: LocalDate
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")

    try {
      date = LocalDate.parse(dateStr, formatter)
    } catch (e: DateTimeParseException) {
      throw IllegalArgumentException("Invalid date format ${e.message}!")
    }

    val previousDay = date.plusDays(1)
    val AVFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return previousDay.format(AVFormatter)
  }

  @OptIn(ExperimentalSerializationApi::class)
  suspend fun fetchData(
    date: String,
    symbol: String,
  ): Map<String, PricesDaily>
  {
    val apiKey = ConfigManager().getAPIKey("AlphaVantage")
    val frequency = "TIME_SERIES_DAILY"

    val client = HttpClient(CIO) {
      install(ContentNegotiation) {
          json(Json { ignoreUnknownKeys = true }) // Safely handles unknown fields
      }
    }
    val customJson = Json { ignoreUnknownKeys = true }

    // if difference between dates > 100 then use outputsize "full"
    // val outputSize = "compact"    // "full"
    val outputSize = "full"

    val finDataProviderURL = "https://www.alphavantage.co/query"
    // "&interval=" + interval +  not required for 'TIME_SERIES_DAILY'
    val url = finDataProviderURL + "?function=" + frequency + "&symbol=" +
      symbol + "&outputsize="+ outputSize + "&apikey=" + apiKey

    if (mocking) {
      // Deserialize JSON into the TimeSeriesDaily class
      val tsd = customJson.decodeFromString<TimeSeriesDaily>(mJsonText)
      return tsd.prices
    }

    var partialText = ""   // for debugging during an Exception

    try {
      val jsonText = client.get(url).bodyAsText()

      val responseHead = jsonText.take(500)
      // println("API Response partial: " + responseHead)
      partialText = jsonText.substringAfter("Time Series (Daily)").takeLast(450)
      // println("Received data tail: " + partialText)

      // Deserialize JSON into the TimeSeriesDaily class
      val tsd = customJson.decodeFromString<TimeSeriesDaily>(jsonText)

      // comment for debugging with local data for testing
      if (tsd.metaData.symbol != symbol) {
        // Fatal, break right away
        throw IllegalStateException("Symbol doesn't match on AV API response: $responseHead")
      }

      return tsd.prices
    }
    catch (e: MissingFieldException) {
      if (partialText.contains("our standard API rate limit is 25 requests per day")) {
        // need to fallback to second API
        println("API limit reached! Encountered while requesting data for '\$$symbol'")
      }
      else {
        println("Probably: wrong symbol/ticker on input!")
        println("API Response partial: $partialText")
      }

      println("Details Below, ${e.message}")
      throw e
  } catch (e: Exception) {
        println("Error: ${e.message}")
        throw e
    } finally {
        client.close()
    }
  }

  /**
   * ID = TIME_SERIES_INTRADAY
   * Closing price doesn't match on the last hour with market closing price!
   *
   * TODO: utilize this to get after hours delta
   */
  suspend fun fetchData_ID(
    date: String,
    symbol: String,
    interval: String,
    frequency: String,
    apiKey: String,
  ): Map<String, PricesDaily>?
  { 
    val client = HttpClient(CIO) {
      install(ContentNegotiation) {
          json(Json { ignoreUnknownKeys = true }) // Safely handles unknown fields
      }
    }
    val customJson = Json { ignoreUnknownKeys = true }

    val outputSize = "compact"
    val finDataProviderURL = "https://www.alphavantage.co/query"
    // extract from param, date
    val month = "2024-05"

    val url = finDataProviderURL + "?function=" + frequency + "&symbol=" +
      symbol + "&interval=" + interval + "&month=" + month + "&outputsize=" +
      outputSize +  "&extended_hours=false" + "&apikey=" + apiKey

    if (mocking) {
      // Deserialize JSON into the TimeSeries ID class
      val tsid = customJson.decodeFromString<TimeSeriesID>(mJsonText)
      return tsid.prices
    }

    return try {
      val jsonText = client.get(url).bodyAsText()

      val trimmedText = jsonText.substringAfter("Time Series (60min)")
      println("Received AV data: " + jsonText)

      // Deserialize JSON into the TimeSeriesDaily class
      val tsd = customJson.decodeFromString<TimeSeriesDaily>(jsonText)
      tsd.prices
    } catch (e: Exception) {
        println("Error: ${e.message}")
        null
    } finally {
        client.close()
    }
  }
 }