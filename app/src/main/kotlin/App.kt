/**
 * Author: Atiq Rahman
 *
 * Desc
 * - Take care of input and pass converted asset list to ERManager
 *  - utilize class TableParser to convert to asset list
 *
 */

package FinTool


fun main(args: Array<String>) {
  /**
   * Earlier ERManager constructor was responsible for parsing and processing the
   * CL arguments; now it's on main method and TableParser
   * Create instance of class ERManager and call runAsync()
   */
  if (args.size < 1) {
    println("Not enough arguments!")
    return
  }

  // TODO: add validation for year
  val year = args[0]

  /**
   * Doesn't contain numbers, special chars
   */
  // if (sym.length > 5) {
  //   println("Invalid Symbol on argument!")
  //   return
  // }

  // val date = args[1]
  // check on args 1

  val tableParser = TableParser()
  tableParser.takeInput()
  val assetsTable = tableParser.getAssetList(year)

  println()
  println("ER Table for a $year Q")
  println("---------------------")

  // ERManager().runAsync(sym, date)
  ERManager().runAsync(assetsTable)
}
