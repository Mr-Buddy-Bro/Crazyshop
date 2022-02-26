package com.tech2develop.crazyshop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class BuyerHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_home)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}