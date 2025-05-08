/**
 * Parses input tables in markdown format for input
 * Generate tables in output format
 *  - maintains order in output table that was in input table
 */

package FinTool

class TableParser {
  private var tokens: List<String> = emptyList()

  fun takeInput(
  ) {
    println("Enter input table:")
    var inputTableStr = generateSequence(::readLine).joinToString("\n")
    
    // Should not be required, should be able to handle it using newLineFound
    //  and isFirstColumn
    /*
    var titleRowTrail = "--- |\n"
    if (inputTableStr.contains(titleRowTrail)) {
      inputTableStr = inputTableStr.substringAfter(titleRowTrail)
      println("Header row removed.")
      println("trimmed str: \n $inputTableStr")
    }*/

    tokens = inputTableStr.split("|") // Multiple delimiters
  }

  /**
   * Convert 
   */
  fun getAssetList(
    year: String,
  ): MutableMap<String, MutableList<String>>
  {
    val assets: MutableMap<String, MutableList<String>> = mutableMapOf()

    val monthRegex1 = Regex("^0\\d-\\d\\d$")
    val monthRegex2 = Regex("^1[0-2]-\\d\\d$")
    var newLineFound = true
    var isFirstColumn = false
    // current running date for the next symbols
    var date: String = ""

    tokens.forEach { token -> 
      if (token.isNotEmpty()) {
        // trimmed / cleaned token
        val cToken = token.trim()
        //  && token.isNotBlank()

        if (newLineFound) {
          if (cToken.matches(monthRegex1) || cToken.matches(monthRegex2)) {
            date = cToken + "-" + year
          }
  
          if (isFirstColumn == false)
            isFirstColumn = true

          newLineFound = false
        }
        else if (isFirstColumn) {
          isFirstColumn = false

          // TODO: add code to validate symbol to ignore tokes that might be
          //  present on the first or second row without header row removed
          val symbol = cToken
          if (symbol.isNotEmpty() && symbol.contains('-') == false) {
            if (date.isEmpty() || date.isBlank())
              throw IllegalArgumentException("key is empty/blank")

            if (assets.containsKey(date) == false)
              assets.put(date, mutableListOf())

            assets[date]?.add(symbol)
          }
        }

        if (token.equals("\n"))
          newLineFound = true
      }
     }
     
     return assets
  }
}
