package com.example.finalinvestmentapp

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseStocks : AppCompatActivity(), BrowseAdapter.BrowseAdapterListener {
    private lateinit var dbHelper: StocksDBHelper
    private lateinit var symbolsCursor: Cursor
    private lateinit var symbolsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var browseAdapter: BrowseAdapter
    private val priceCache = HashMap<String, Stock>()

    private var offset = 0
    private var waiting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_stocks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.appToolbar)
        setSupportActionBar(toolbar)

        progressBar = findViewById<ProgressBar>(R.id.progressBar2)

        dbHelper= StocksDBHelper(this)
        symbolsCursor = dbHelper.getAllSymbolsCursor()

        symbolsRecyclerView = findViewById(R.id.symbolsRecyclerView)
        symbolsRecyclerView.layoutManager = LinearLayoutManager(this)
        browseAdapter = BrowseAdapter(symbolsCursor, priceCache, this)
        symbolsRecyclerView.adapter = browseAdapter


        // Is this a bad pattern?
        /*
        symbolsRecyclerView.setOnScrollChangeListener { view, x, y, ox, oy ->
            val dx = ox - x
            val dy = oy - y
            Log.d("Delta y", dy.toString())
            Log.d("Delta x", dx.toString())
        }
         */

        fetchRecords()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.browse_stocks_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        searchSymbolDialog()
        return true
    }

    fun buyDialog(symbol: String, price: Double?){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Buy $symbol")

        val dialogLayout = LinearLayout(this)
        dialogLayout.orientation = LinearLayout.VERTICAL
        dialogLayout.setPadding(40, 12, 40, 0)

        val priceText = if(price != null) {
            "$%.2f".format(price)
        } else {
            "price unavailable"
        }
        val balance = User.loggedInUser?.balance
        val balanceText = if(balance != null) {
            "$%.2f".format(balance)
        } else {
            "balance unavailable"
        }

        val priceTextView = TextView(this)
        priceTextView.text = "Current price: $priceText"
        dialogLayout.addView(priceTextView)

        val balanceTextView = TextView(this)
        balanceTextView.text = "Current balance: $balanceText"
        dialogLayout.addView(balanceTextView)

        val quantityEditText = EditText(this)
        quantityEditText.hint = "Enter quantity"
        dialogLayout.addView(quantityEditText)
        builder.setView(dialogLayout)

        builder.setPositiveButton("Buy") { dialog, which ->
            val quantity = quantityEditText.text.toString().toDoubleOrNull()
            if(quantity == null || quantity <= 0.0){
                Toast.makeText(applicationContext, "Invalid quantity", Toast.LENGTH_SHORT).show()
            } else {
                if(quantity*price!! > balance!!){
                    Toast.makeText(applicationContext, "Insufficent Balance", Toast.LENGTH_SHORT).show()
                }else {
                    User.loggedInUser?.balance -= quantity*price
                    dbHelper.buyHolding(symbol, quantity)
                    Toast.makeText(
                        applicationContext,
                        "Purchased$quantity $symbol",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    fun searchSymbolDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search For a Symbol")

        val symbolEditText = EditText(this)
        symbolEditText.hint = "Enter stock symbol"
        builder.setView(symbolEditText)

        builder.setPositiveButton("Search") { dialog, which ->
            val symbol = symbolEditText.text.toString().trim().uppercase()
            if (symbol.isEmpty()) {
                Toast.makeText(applicationContext, "Invalid entry", Toast.LENGTH_SHORT).show()
            } else {
                val index = dbHelper.getSymbolIndex(symbol)
                if (index == -1){
                    Toast.makeText(applicationContext, "Symbol Not Found", Toast.LENGTH_SHORT).show()
                }else {
                    offset = index
                    priceCache.clear()
                    browseAdapter.startIndex = index
                    browseAdapter.visibleItemCount = 0
                    fetchRecords()
                    symbolsRecyclerView.scrollToPosition(0)
                }
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


        fun fetchRecords() {
            if (waiting) {
                return
            }
            progressBar.visibility = View.VISIBLE
            waiting = true
            lifecycleScope.launch {
                val symbols = dbHelper.getSymbols(offset, 20)
                for (symbol in symbols) {
                    val stock = StockAPIHelper.getStock(symbol)
                    if (stock != null) {
                        priceCache[symbol] = stock
                    }
                }
                offset += symbols.size
                browseAdapter.visibleItemCount += symbols.size
                browseAdapter.notifyDataSetChanged()
                withContext(Dispatchers.IO) {
                    delay(5000)
                }

                waiting = false
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }



    override fun onDestroy() {
        symbolsCursor.close()
        super.onDestroy()
    }

    override fun onClick(symbol: String, price: Double?) {
        buyDialog(symbol, price)
    }

    override fun onBottomReached(position: Int) {
        fetchRecords()
    }
}
