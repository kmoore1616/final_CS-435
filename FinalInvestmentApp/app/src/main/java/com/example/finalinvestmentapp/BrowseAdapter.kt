package com.example.finalinvestmentapp

import android.database.Cursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BrowseAdapter(private val cursor: Cursor, private val priceCache: HashMap<String, Stock>, private val browseAdapterListener: BrowseAdapterListener
) : RecyclerView.Adapter<BrowseAdapter.ViewHolder>() {
    var startIndex = 0
    var visibleItemCount = 0


    interface BrowseAdapterListener{
        fun onClick(symbol: String, price: Double?)
        fun onBottomReached(position: Int)
    }


    class ViewHolder(itemView: View, private val browseAdapterListener: BrowseAdapterListener) : RecyclerView.ViewHolder(itemView) {
        private val symbolTextView: TextView = itemView.findViewById(R.id.prvStockNameTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.prvStockPriceTextView)
        private val percentChangeTextView: TextView = itemView.findViewById(R.id.prvStockPercentChangeTextView)
        private var symbol: String = ""
        private var price: Double? = null

        init {
            itemView.setOnClickListener {
                browseAdapterListener.onClick(symbol, price)
            }
        }

        fun update(symbol: String, stock: Stock?) {
            this.symbol = symbol
            this.price = stock?.price
            symbolTextView.text = symbol
            var outText: String
            if (stock != null){
                outText = "%.2f".format(stock.price)
                percentChangeTextView.text = "%.2f%%".format(stock.percentChange)
                val percentChange = stock.percentChange?: 0.0
                if(percentChange >= 0.0){
                    percentChangeTextView.setTextColor(Color.GREEN)
                } else {
                    percentChangeTextView.setTextColor(Color.RED)
                }
            } else {
                outText = "--"
                percentChangeTextView.text = "--"
                percentChangeTextView.setTextColor(Color.GRAY)
            }
            priceTextView.text = outText
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_recyclerview, parent, false)
        return ViewHolder(view, browseAdapterListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == priceCache.size -1){
            browseAdapterListener.onBottomReached(position)
        }
        val cursorPosition = startIndex +  position
        if (cursor.moveToPosition(cursorPosition)) {
            val symbol = cursor.getString(cursor.getColumnIndexOrThrow("symbol"))
            val stock = priceCache[symbol]
            holder.update(symbol, stock)
        }
    }

    override fun getItemCount(): Int {
        return visibleItemCount
    }
}
