package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class UserType : AppCompatActivity() {

    lateinit var userType : String
    lateinit var tvMode : TextView
    lateinit var tvDesc : TextView
    lateinit var btnExisting : MaterialButton
    lateinit var btnNew : MaterialButton
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_type)

        userType = intent.getStringExtra("type").toString()
        auth = FirebaseAuth.getInstance()


        tvMode = findViewById(R.id.textView)
        tvDesc = findViewById(R.id.text23)
        btnExisting = findViewById(R.id.materialButton3)
        btnNew = findViewById(R.id.btnNewUser)

        setContent()
        btnExisting.setOnClickListener {
            val i = Intent(this,LoginActivity::class.java)
            i.putExtra("type", userType)
            startActivity(i)
        }
        btnNew.setOnClickListener {
            if (userType.equals("seller")) {
                startActivity(Intent(this, SellerRegistration::class.java))
            }else{
                startActivity(Intent(this, BuyerRegistration::class.java))
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            if (userType.equals("seller")) {
                startActivity(Intent(this, SellerHome::class.java))
            }else{
                startActivity(Intent(this, BuyerHome::class.java))
            }
        }
    }

    private fun setContent() {
        if (userType.equals("seller")){
            tvMode.text = "Seller mode"
            tvDesc.text = "Start/Continue selling your products through BenMart"
            btnExisting.text = "Existing seller"
            btnNew.text = "New seller"
        }else{
            tvMode.text = "Buyer mode"
            tvDesc.text = "Start/Continue buying products through BenMart"
            btnExisting.text = "Existing buyer"
            btnNew.text = "New buyer"
        }
    }
}