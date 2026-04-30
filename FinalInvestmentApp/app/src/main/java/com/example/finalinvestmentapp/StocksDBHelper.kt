package com.example.finalinvestmentapp

import android.content.ContentValues
import android.content.Context
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

    fun saveHolding(holding: Holding){
        val db = writableDatabase
        val holdingToSave = ContentValues().apply {
            put(COLUMN_SYMBOL, holding.symbol)
            put(COLUMN_QUANTITY, holding.quantity)
        }
        db.replace(TABLE_HOLDINGS, null, holdingToSave)
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

    fun getAllHoldings(): ArrayList<Holding>{
        val db = readableDatabase
        val holdingArray = arrayListOf<Holding>()
        val cursor = db.query(
            TABLE_HOLDINGS,
            arrayOf(COLUMN_SYMBOL, COLUMN_QUANTITY),
            null,
            null,
            null,
            null,
            COLUMN_SYMBOL
        )
        while(cursor.moveToNext()){
            val holding = Holding(
                cursor.getString(0),
                cursor.getDouble(1)
            )
            holdingArray.add(holding)
        }
        cursor.close()
        return holdingArray
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HOLDINGS")
        onCreate(db)
    }

}
