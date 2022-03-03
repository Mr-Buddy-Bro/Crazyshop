package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var userType: String
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        userType = intent.getStringExtra("type").toString()
        val title = findViewById<TextView>(R.id.tvLoginHead)
        title.text = "Login as ${userType}"

    }

    fun btnLogin(view: android.view.View) {
        val email = findViewById<EditText>(R.id.etLoginEmail).text.toString()
        val password = findViewById<EditText>(R.id.etLoginPassword).text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Invalid inputs",Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    if (userType.equals("seller")) {
                        startActivity(Intent(this, SellerHome::class.java))
                    }else{

                    }
                    finish()
                }else{
                    Toast.makeText(this,"Invalid credentials",Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}