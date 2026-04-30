package com.example.finalinvestmentapp

import android.database.Cursor
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Portfolio : AppCompatActivity() {
    private lateinit var stocksDBHelper: StocksDBHelper
    private lateinit var holdingsRecyclerView: RecyclerView
    private lateinit var holdingsCursor: Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_portfolio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        stocksDBHelper = StocksDBHelper(this)
        holdingsCursor = stocksDBHelper.getAllHoldingsCursor()
        holdingsRecyclerView = findViewById(R.id.holdingsRecyclerView)
        holdingsRecyclerView.layoutManager = LinearLayoutManager(this)
        holdingsRecyclerView.adapter = PortfolioAdapter(holdingsCursor)
    }

    override fun onDestroy() {
        holdingsCursor.close()
        super.onDestroy()
    }
}
