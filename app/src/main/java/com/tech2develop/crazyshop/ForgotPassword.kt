package com.tech2develop.crazyshop

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    lateinit var etResEmail : EditText
    lateinit var btnResContinue : MaterialButton
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etResEmail = findViewById(R.id.etResEmail)
        btnResContinue = findViewById(R.id.btnContinuePass)

        auth = FirebaseAuth.getInstance()

        btnResContinue.setOnClickListener {
            val email = etResEmail.text.toString().trim()

            if (email == ""){
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_LONG).show()
            }else{
                sentResetLink(email)
            }
        }

    }

    private fun sentResetLink(email: String) {
        val loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        loadingDialog.show()
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            loadingDialog.dismiss()
            if(it.isSuccessful){
                Log.d("reset", "sentResetLink: link send")
                Toast.makeText(this, "Password reset link send to $email", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show()
            }
        }
    }
}