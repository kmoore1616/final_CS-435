package com.example.finalinvestmentapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/* Backend DB
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column("username", db.String(100), unique=True, nullable=True)
    password = db.Column("password", db.String(100), nullable=True)
    balance = db.Column("balance", db.Float, nullable=False)

class Holding(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("user.id"), nullable=False)
    symbol = db.Column("symbol", db.String(100), nullable=False)
    quantity = db.Column("quantity", db.Float, nullable=False)

 */

class StocksDBHelper (context: Context):
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
    // Prevents name mixups
    companion object{
        private const val DB_NAME = "holdings.sqlite"
        private const val DB_VERSION = 1
        private const val TABLE_HOLDINGS = "holdings"
        private const val TABLE_SYMBOLS = "symbols"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_SYMBOL = "symbol"
        private const val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val query = """
            CREATE TABLE $TABLE_HOLDINGS(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SYMBOL TEXT NOT NULL UNIQUE,
                $COLUMN_QUANTITY REAL NOT NULL
            )
        """.trimIndent()
        db?.execSQL(query)

        val query2 = """
            CREATE TABLE $TABLE_SYMBOLS(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SYMBOL TEXT NOT NULL UNIQUE
            )
        """.trimIndent()
        db?.execSQL(query2)
    }

    fun isEmpty(): Boolean{
        val db = readableDatabase
        val cursor = db.query(
            TABLE_HOLDINGS,
            arrayOf(COLUMN_ID),
            null,
            null,
            null,
            null,
            null,
            "1"
        )

        val empty= cursor.count == 0
        cursor.close()
        return empty
    }

    fun getSymbolIndex(symbol: String): Int{
        val db = readableDatabase

        val existsCursor = db.query(
            TABLE_SYMBOLS,
            arrayOf(COLUMN_ID),
            "$COLUMN_SYMBOL = ?",
            arrayOf(symbol),
            null,
            null,
            null,
            "1"
        )
        if(!existsCursor.moveToFirst()){
            existsCursor.close()
            return -1
        }
        val searchCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_SYMBOLS WHERE $COLUMN_SYMBOL < ?",
            arrayOf(symbol)
            )
        searchCursor.moveToFirst()
        val index = searchCursor.getInt(0)
        searchCursor.close()
        existsCursor.close()
        return index
    }


    fun isSymbolEmpty(): Boolean{
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SYMBOLS,
            arrayOf(COLUMN_ID),
            null,
            null,
            null,
            null,
            null,
            "1"
        )
        val empty= cursor.count == 0
        cursor.close()
        return empty
    }


    fun addSymbol(symbol: String) {
        val db = writableDatabase
        val symbolToSave = ContentValues().apply {
            put(COLUMN_SYMBOL, symbol)
        }
        db.insert(TABLE_SYMBOLS, null, symbolToSave)
    }

    fun buyHolding(symbol: String, quantity: Double) {
        val db = writableDatabase
        val cursor = db.query(
            TABLE_HOLDINGS,
            arrayOf(COLUMN_QUANTITY),
            "$COLUMN_SYMBOL = ?",
            arrayOf(symbol),
            null,
            null,
            null,
            "1"
        )

        if(cursor.moveToFirst()){
            val currentQuantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
            val holdingToUpdate = ContentValues().apply {
                put(COLUMN_QUANTITY, currentQuantity + quantity)
            }
            db.update(TABLE_HOLDINGS, holdingToUpdate, "$COLUMN_SYMBOL = ?", arrayOf(symbol))
        } else {
            val holdingToAdd = ContentValues().apply {
                put(COLUMN_SYMBOL, symbol)
                put(COLUMN_QUANTITY, quantity)
            }
            db.insert(TABLE_HOLDINGS, null, holdingToAdd)
        }
        cursor.close()
    }

    fun replaceHoldings(holdings: List<Holding>){
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_HOLDINGS, null, null)
            holdings.forEach { holding ->
                val holdingToSave = ContentValues().apply {
                    put(COLUMN_SYMBOL, holding.symbol)
                    put(COLUMN_QUANTITY, holding.quantity)
                }
                db.insert(TABLE_HOLDINGS, null, holdingToSave)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun clearHoldings(){
        val db = writableDatabase
        db.delete(TABLE_HOLDINGS, null, null)
    }

    fun getAllHoldingsCursor(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_HOLDINGS,
            arrayOf(COLUMN_ID, COLUMN_SYMBOL, COLUMN_QUANTITY),
            null,
            null,
            null,
            null,
            COLUMN_SYMBOL
        )
    }

    fun getAllSymbolsCursor(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_SYMBOLS,
            arrayOf(COLUMN_ID, COLUMN_SYMBOL),
            null,
            null,
            null,
            null,
            COLUMN_SYMBOL
        )
    }


    fun getSymbols(offset: Int, limit: Int): ArrayList<String> {
        val db = readableDatabase
        val symbols = arrayListOf<String>()
        val cursor = db.query(
            TABLE_SYMBOLS,
            arrayOf(COLUMN_SYMBOL),
            null,
            null,
            null,
            null,
            COLUMN_SYMBOL,
            "$offset, $limit"
        )

        while (cursor.moveToNext()) {
            symbols.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SYMBOL)))
        }
        cursor.close()
        return symbols
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

}
