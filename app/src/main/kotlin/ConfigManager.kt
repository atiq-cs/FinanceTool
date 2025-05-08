/**
 * Should be key value pairs in config file for each API Key
 *
 * Notes: Utilize nio instead of java.io
 *
 * Refs:
 *  1. on XDG_CONFIG_HOME
 *      https://specifications.freedesktop.org/basedir-spec/latest
 */

package FinTool

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class ConfigManager {
  fun getUserConfigDir(): String {
    val configHome = System.getenv("XDG_CONFIG_HOME")
    return if (!configHome.isNullOrBlank()) {
      configHome
    } else {
      Paths.get(System.getProperty("user.home"), ".config").toString()
    }
  }

/**
 * Ensure method does not return null, throw an exception instead
 */
  suspend fun getAPIKey(
    providerName: String
  ): String
  {
    // xdg ref on notes above
    val configFilePath: Path = Paths.get(getUserConfigDir(), "FinTool", "config.json")

    if (!Files.exists(configFilePath)) {
        println("Config file not found at: $configFilePath")
        return "ERROR"
    }

    // Read the file using java.nio
    val jsonString = Files.readString(configFilePath)

    // Deserialize JSON to key-value pairs
    val configMap: Map<String, String> = Json.decodeFromString(jsonString)
    val apiKey = configMap[providerName]
    if (apiKey == null)
      throw NullPointerException("ConfigManager: API Key is null!")

    return apiKey
  }
}