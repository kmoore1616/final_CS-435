package com.example.finalinvestmentapp

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
    private var holdingsCursor: Cursor? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var balanceTextView: TextView
    private lateinit var portfolioAdapter: PortfolioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_portfolio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        balanceTextView = findViewById<TextView>(R.id.balanceTextView)
        updateBalance()
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        browseStocksButton = findViewById(R.id.browseStocksButton)
        val toolbar = findViewById<Toolbar>(R.id.appToolbar)
        setSupportActionBar(toolbar)

        stocksDBHelper = StocksDBHelper(this)

        fetchSymbolsIfNotExist()

        holdingsCursor = stocksDBHelper.getAllHoldingsCursor()
        holdingsRecyclerView = findViewById(R.id.holdingsRecyclerView)
        holdingsRecyclerView.layoutManager = LinearLayoutManager(this)
        portfolioAdapter = PortfolioAdapter(holdingsCursor!!)
        holdingsRecyclerView.adapter = portfolioAdapter
    }

    override fun onResume() {
        super.onResume()
        val newCursor = stocksDBHelper.getAllHoldingsCursor()
        val oldCursor = holdingsCursor
        holdingsCursor = newCursor
        portfolioAdapter.swapCursor(newCursor)
        oldCursor?.close()
        updateBalance()
    }

    fun updateBalance(){
        balanceTextView.text = "Balance: ${User.loggedInUser?.balance.toString()}"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.portfolio_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        depositWithdrawDialog()
        return true
    }

    fun depositWithdrawDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Deposit/Withdraw Funds")

        val numEditText = EditText(this)
        numEditText.hint = "Enter amound in USD"

        builder.setView(numEditText)

        builder.setPositiveButton("Deposit") { dialog, which ->
            val amount = numEditText.text.toString().toFloatOrNull()
            if (amount == null || amount <= 0){
                Toast.makeText(applicationContext, "Invalid entry", Toast.LENGTH_SHORT).show()
            }else{
                User.loggedInUser?.balance += amount
                updateBalance()
                Toast.makeText(applicationContext, "Deposited \$$amount", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Withdraw") {dialog, which ->
            val amount = numEditText.text.toString().toFloatOrNull()
            if (amount == null || amount <= 0){
                Toast.makeText(applicationContext, "Invalid entry", Toast.LENGTH_SHORT).show()
            }else{
                User.loggedInUser?.balance -= amount
                updateBalance()
                Toast.makeText(applicationContext, "Withdrew \$$amount", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNeutralButton("Cancel", null)
        builder.show()
    }

    fun browseClick(view: View){
        startActivity(Intent(applicationContext, BrowseStocks::class.java))
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
        holdingsCursor?.close()
        super.onDestroy()
    }

}
