package com.example.finalinvestmentapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class StockAPIHelper {
    companion object {
        private const val BASE_URL = "https://finnhub.io/api/v1"
        private const val API_KEY = "d7em1v1r01qi33g6qipgd7em1v1r01qi33g6qiq0"
        private const val PAGE_SIZE = 20
        private var sp500StocksSortedByPrice: ArrayList<Stock>? = null

        suspend fun getStockPrice(symbol: String): Double? {
            var price: Double? = null
            withContext(Dispatchers.IO) {
                var connection: HttpURLConnection?
                connection = null
                try {
                    val targetUrl = "$BASE_URL/quote?symbol=$symbol&token=$API_KEY"
                    val url = URL(targetUrl)
                    Log.d("url", url.toString())
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val inputStream = connection.getInputStream()
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val response = bufferedReader.readText()
                    bufferedReader.close()
                    Log.d("request", response)

                    val stockJSON = JSONObject(response)
                    price = stockJSON.getDouble("c")
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                }
            }
            return price
        }

        suspend fun fetchSymbols(dbHelper: StocksDBHelper): Boolean {
            var success = false
            withContext(Dispatchers.IO) {
                var connection: HttpsURLConnection?
                connection = null
                try {
                    //https://finnhub.io/api/v1/stock/symbol?exchange=US&token=d7em1v1r01qi33g6qipgd7em1v1r01qi33g6qiq0
                    val targetUrl = "$BASE_URL/stock/symbol?exchange=US&token=$API_KEY"
                    val url = URL(targetUrl)
                    connection = url.openConnection() as HttpsURLConnection
                    val inputStream = connection.getInputStream()
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val response = JSONArray(bufferedReader.readText())
                    bufferedReader.close()

                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val symbol = obj.getString("symbol")
                        dbHelper.addSymbol(symbol)
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                }
            }
            return false
        }
    }
}