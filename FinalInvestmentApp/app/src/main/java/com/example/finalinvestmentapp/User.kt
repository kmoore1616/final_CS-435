package com.example.finalinvestmentapp

class User(val username: String, var balance : Double) {
    companion object {
        var loggedInUser: User? = null
    }
}