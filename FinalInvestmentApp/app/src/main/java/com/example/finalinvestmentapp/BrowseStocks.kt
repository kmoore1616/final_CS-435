package com.example.finalinvestmentapp

import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BrowseStocks : AppCompatActivity() {
    private lateinit var stocksDBHelper: StocksDBHelper
    private lateinit var symbolsCursor: Cursor
    private lateinit var symbolsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_stocks)

        stocksDBHelper = StocksDBHelper(this)
        symbolsCursor = stocksDBHelper.getAllSymbolsCursor()

        symbolsRecyclerView = findViewById(R.id.symbolsRecyclerView)
        symbolsRecyclerView.layoutManager = LinearLayoutManager(this)
        symbolsRecyclerView.adapter = BrowseAdapter(symbolsCursor)
    }

    override fun onDestroy() {
        symbolsCursor.close()
        super.onDestroy()
    }
}
