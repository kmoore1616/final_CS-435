package com.example.finalinvestmentapp

import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Portfolio : AppCompatActivity() {
    private lateinit var stocksDBHelper: StocksDBHelper
    private lateinit var holdingsRecyclerView: RecyclerView
    private lateinit var browseStocksButton: Button
    private lateinit var holdingsCursor: Cursor
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_portfolio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        browseStocksButton = findViewById(R.id.browseStocksButton)

        stocksDBHelper = StocksDBHelper(this)

        fetchSymbolsIfNotExist()

        holdingsCursor = stocksDBHelper.getAllHoldingsCursor()
        holdingsRecyclerView = findViewById(R.id.holdingsRecyclerView)
        holdingsRecyclerView.layoutManager = LinearLayoutManager(this)
        holdingsRecyclerView.adapter = PortfolioAdapter(holdingsCursor)



    }

    fun fetchSymbolsIfNotExist(){
        lifecycleScope.launch {
            withContext(Dispatchers.Main){
                progressBar.visibility = View.VISIBLE
            }
            withContext(Dispatchers.IO){
                if(stocksDBHelper.isSymbolEmpty()){
                    StockAPIHelper.fetchSymbols(stocksDBHelper)
                }
            }

            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }

        }
    }

    override fun onDestroy() {
        holdingsCursor.close()
        super.onDestroy()
    }

}
