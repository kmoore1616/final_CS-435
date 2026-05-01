package com.example.finalinvestmentapp

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PortfolioAdapter(
    private var holdingsCursor: Cursor
) : RecyclerView.Adapter<PortfolioAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symbolTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val quantityTextView: TextView = itemView.findViewById(android.R.id.text2)

        fun update(cursor: Cursor) {
            val symbol = cursor.getString(cursor.getColumnIndexOrThrow("symbol"))
            val quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"))

            symbolTextView.text = symbol
            quantityTextView.text = quantity.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holdingsCursor.moveToPosition(position)) {
            holder.update(holdingsCursor)
        }
    }

    override fun getItemCount(): Int {
        return holdingsCursor.count
    }

    fun swapCursor(newCursor: Cursor) {
        holdingsCursor = newCursor
        notifyDataSetChanged()
    }
}
