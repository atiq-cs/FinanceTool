/**
 * Abstract to layer to provide access to market data APIs
 *  - Provider Implementation agnostic
 *  - layer on top of Finance APIs
 * 
 * Available Ops
 * - get output table based on input table (extract delta for specific days)
 * - compute premium
 */

package FinTool


class UnifiedDataProvider {
  suspend fun getClosingPricePair(
    date: String,
    symbol: String,
  ): ClosingPair
  {
    /**
     * Alpha Vantage Section
     *  Utlize local json data for initial probe
     */

    return AlphaVantage().getClosingPricePair(date, symbol)
  }
}