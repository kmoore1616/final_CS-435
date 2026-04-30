package com.example.finalinvestmentapp

class Holding(val symbol: String, var quantity: Double, var transactions: ArrayList<Double> = arrayListOf()) {
    companion object{
        fun calculateProfitLoss(holding: Holding, currentStockPrice: Double): Double{
            var profitLoss = 0.0
            val totalSpent = holding.transactions.sum()
            if(totalSpent == 0.0){
                return 0.0
            }
            for(transaction in holding.transactions){
                profitLoss +=  currentStockPrice - transaction
            }
            profitLoss /= totalSpent
            return profitLoss
        }
    }
}
