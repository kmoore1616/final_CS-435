package com.example.finalinvestmentapp

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BrowseAdapter(
    private val cursor: Cursor
) : RecyclerView.Adapter<BrowseAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symbolTextView: TextView = itemView.findViewById(R.id.prvStockNameTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.prvStockPriceTextView)

        fun update(cursor: Cursor) {
            symbolTextView.text = cursor.getString(cursor.getColumnIndexOrThrow("symbol"))
            priceTextView.text = "--"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stock_recyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (cursor.moveToPosition(position)) {
            holder.update(cursor)
        }
    }

    override fun getItemCount(): Int {
        return cursor.count
    }
}
