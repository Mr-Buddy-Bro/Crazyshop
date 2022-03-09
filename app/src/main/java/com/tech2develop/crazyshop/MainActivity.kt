package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.tech2develop.crazyshop.Models.SellerModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun btnSeller(view: android.view.View) {
        switchActivity("seller")
    }
    fun btnBuyer(view: android.view.View) {
        switchActivity("buyer")
    }

    fun switchActivity(type :String){
        val i = Intent(this,UserType::class.java)
        i.putExtra("type", type)
        startActivity(i)
    }
}