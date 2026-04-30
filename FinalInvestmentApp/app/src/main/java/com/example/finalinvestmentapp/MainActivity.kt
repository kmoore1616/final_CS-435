package com.example.finalinvestmentapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var errorField: TextView
    private lateinit var stocksDBHelper: StocksDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usernameField = findViewById<EditText>(R.id.usernameEditText)
        passwordField = findViewById<EditText>(R.id.passwordEditText)
        errorField = findViewById<TextView>(R.id.logResultTextView)
        stocksDBHelper = StocksDBHelper(this)

    }

    fun onLoginClick(view: View) {
        lifecycleScope.launch {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                errorField.visibility = View.VISIBLE
                errorField.text = getString(R.string.pass_err)
            } else {
                val user = BackendAPIHelper.authenticateUser(username,password)
                if (user != null) {
                    withContext(Dispatchers.IO) {
                        stocksDBHelper.replaceHoldings(Holding.tempHoldingArray)
                    }
                    errorField.visibility = View.GONE
                } else {
                    errorField.visibility = View.VISIBLE
                    errorField.text = getString(R.string.login_err)
                }
            }
        }
    }
    fun onSignupClick(view: View){
        lifecycleScope.launch {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                errorField.visibility = View.VISIBLE
                errorField.text = getString(R.string.pass_err)
            } else {
                BackendAPIHelper.createUser(username,password)
            }
        }

    }

}
