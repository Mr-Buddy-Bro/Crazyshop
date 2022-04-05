package com.tech2develop.crazyshop

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    lateinit var userType: String
    lateinit var auth : FirebaseAuth
    lateinit var loadingDialog : Dialog
    lateinit var btnForgotPass : TextView
    lateinit var tvErrorLogin : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        btnForgotPass = findViewById(R.id.btnForgotPass)

        tvErrorLogin = findViewById<TextView>(R.id.tvErrorLogin)

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        userType = intent.getStringExtra("type").toString()
        val title = findViewById<TextView>(R.id.tvLoginHead)
        title.text = "Login as ${userType}"

        btnForgotPass.setOnClickListener {
            startActivity(Intent(applicationContext, ForgotPassword::class.java))
        }

    }

    fun btnLogin(view: android.view.View) {
        val email = findViewById<EditText>(R.id.etLoginEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etLoginPassword).text.toString()

        if (email.isEmpty() || password.isEmpty()){
            tvErrorLogin.text = "Invalid inputs"
            tvErrorLogin.visibility = View.VISIBLE
        }else{
            var existing = false
            val firestore = FirebaseFirestore.getInstance()

            if (userType == "seller")
            {

                firestore.collection("Buyer").get().addOnCompleteListener {
                    if (it.isSuccessful){
                        for (doc in it.result!!){
                            if (doc.id == email){
                                existing = true
                                 break
                            }

                        }
                        if(existing){
                            val tv = findViewById<TextView>(R.id.tvErrorLogin)
                            tv.text = "The Email id is already used as Buyer , try another email address"
                            tv.visibility = View.VISIBLE
                        }
                        else {
                            login(email,password)
                        }

                    }
                }
            }
            else if (userType == "buyer")
            {
                firestore.collection("Seller").get().addOnCompleteListener {
                    if (it.isSuccessful){
                        for (doc in it.result!!){
                            if (doc.id == email){
                                existing = true
                                break
                            }
                        }
                        if(existing){
                            val tv = findViewById<TextView>(R.id.tvErrorLogin)
                            tv.text = "The Email id is already used as Seller , try another email address"
                            tv.visibility = View.VISIBLE
                        }
                        else{
                            login(email,password)
                        }

                    }
                }
            }}




    }

    private fun login(email: String, password: String) {
        val tv = findViewById<TextView>(R.id.tvErrorLogin)
        tv.text = ""
        tv.visibility = View.INVISIBLE
        loadingDialog.show()
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
            loadingDialog.dismiss()
            if (it.isSuccessful){
                if (userType.equals("seller")) {
                    auth.currentUser!!.reload().addOnCompleteListener {
                        if (auth.currentUser!!.isEmailVerified){
                            val i = Intent(this, SellerHome::class.java)
                            startActivity(i)
                        }else{
                            startActivity(Intent(this, VerifyEmailActivity::class.java))
                            finish()
                        }
                    }
                }else{
                    auth.currentUser!!.reload().addOnCompleteListener {
                        if (auth.currentUser!!.isEmailVerified){
                            val i = Intent(this, BuyerHome::class.java)
                            startActivity(i)
                        }else{
                            startActivity(Intent(this, VerifyEmailActivity::class.java))
                            finish()
                        }
                    }
                }

            }else{
                tvErrorLogin.text = "Invalid credentials"
                tvErrorLogin.visibility = View.VISIBLE
                Toast.makeText(this,"Invalid credentials",Toast.LENGTH_LONG).show()
            }
        }
    }}
