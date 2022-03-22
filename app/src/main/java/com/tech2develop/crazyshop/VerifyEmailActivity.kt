package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_buyer_email)

        sendVerification()
        checkVerified()
    }

    fun sendVerification(){
        auth = FirebaseAuth.getInstance()
        auth.currentUser!!.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(this, "Email verification send to ${auth.currentUser!!.email}", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Failed to send verification. Please try again later", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkVerified() {
        auth.currentUser!!.reload().addOnCompleteListener {
            if (auth.currentUser!!.isEmailVerified){
                val i = Intent(this, BuyerHome::class.java)
                startActivity(i)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        checkVerified()
    }

    fun btnResentVerification(view: View) {
        sendVerification()
    }
}