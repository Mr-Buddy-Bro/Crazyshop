package com.tech2develop.crazyshop

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tech2develop.crazyshop.Models.SellerModel

class MainActivity : AppCompatActivity() {

    var lastLoginAs : String? = null
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val sharedPreferences: SharedPreferences = getSharedPreferences("userType", MODE_PRIVATE)
        lastLoginAs = sharedPreferences.getString("type","akhil")
        Log.d("sharedPreference", lastLoginAs!!)

    }

    fun btnSeller(view: android.view.View) {
        if (lastLoginAs.equals("Buyer") && auth.currentUser != null){
                Toast.makeText(this, "Please logout from Buyer account before, to start Seller mode", Toast.LENGTH_LONG).show()
        }else {
            switchActivity("seller")
        }
    }
    fun btnBuyer(view: android.view.View) {
        if (lastLoginAs.equals("Seller") && auth.currentUser != null){
                Toast.makeText(this, "Please logout from Seller account before, to start Buyer mode", Toast.LENGTH_LONG).show()
        }else {
            switchActivity("buyer")
        }
    }

    fun switchActivity(type :String){
        val sharedPreferences: SharedPreferences = getSharedPreferences("userType", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (type == "seller"){
            editor.putString("type", "Seller")
            editor.apply()
            editor.commit()
            Log.d("sharedPreference", "Seller")
        }else{
            editor.putString("type", "Buyer")
            editor.apply()
            editor.commit()
            Log.d("sharedPreference", "Buyer")
        }

        val i = Intent(this,UserType::class.java)
        i.putExtra("type", type)
        startActivity(i)
    }
}