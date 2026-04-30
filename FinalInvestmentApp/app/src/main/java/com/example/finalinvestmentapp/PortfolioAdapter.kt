package com.example.finalinvestmentapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PortfolioAdapter(
    private val holdings: ArrayList<Holding>
) : RecyclerView.Adapter<PortfolioAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Placeholder item views. Replace android.R.layout.simple_list_item_2
        // with a custom holding row layout when the final UI is ready.
        private val symbolTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val quantityTextView: TextView = itemView.findViewById(android.R.id.text2)

        fun update(holding: Holding) {
            symbolTextView.text = holding.symbol
            quantityTextView.text = holding.quantity.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.update(holdings[position])
    }

    override fun getItemCount(): Int {
        return holdings.size
    }
}
