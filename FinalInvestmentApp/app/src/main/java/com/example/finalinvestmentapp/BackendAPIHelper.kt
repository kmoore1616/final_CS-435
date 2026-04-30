package com.example.finalinvestmentapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BackendAPIHelper {
    companion object {
        suspend fun authenticateUser(username: String, password: String): User? {
            var outUser: User? = null
            withContext(Dispatchers.IO) {
                var connection: HttpURLConnection?
                connection = null
                try {
                    Holding.tempHoldingArray.clear()
                    val ip = "http://10.40.225.73:5000"
                    val targetUrl = "$ip/authenticate_user/$username/$password"
                    val url = URL(targetUrl)
                    Log.d("url", url.toString())
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val inputStream = connection.getInputStream()
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val response = bufferedReader.readText()
                    bufferedReader.close()
                    Log.d("request", response)
                    val userJSON = JSONObject(response)
                    val success = userJSON.getString("authenticated")
                    if(success == "true"){
                        val user = userJSON.getJSONObject("user")
                        val balance = user.getDouble("balance")
                        val username = user.getString("username")
                        outUser = User(username, balance)
                        val holdings = user.getJSONArray("holdings")

                        for(idx in 0 until holdings.length()){
                            val holding = holdings.getJSONObject(idx)
                            val symbol = holding.getString("symbol")
                            val quantity = holding.getDouble("quantity")
                            val holdObject = Holding(symbol, quantity)
                            Holding.tempHoldingArray.add(holdObject)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                }

            }
            return outUser
        }

        suspend fun createUser(username: String, password: String): User? {
            var outUser: User? = null
            withContext(Dispatchers.IO) {
                var connection: HttpURLConnection?
                connection = null
                try {
                    val ip = "http://10.40.225.73:5000"
                    val targetUrl = "$ip/create_user/$username/$password"
                    val url = URL(targetUrl)
                    Log.d("url", url.toString())
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val inputStream = connection.getInputStream()
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))

                    val response = bufferedReader.readText()
                    bufferedReader.close()
                    Log.d("request", response)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                }
            }
            return outUser
        }
    }
}
