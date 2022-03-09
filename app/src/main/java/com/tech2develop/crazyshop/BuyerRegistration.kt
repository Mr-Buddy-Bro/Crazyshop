package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.Models.BuyerModel

class BuyerRegistration : AppCompatActivity() {

    lateinit var etfName : EditText
    lateinit var etlName : EditText
    lateinit var etEmail : EditText
    lateinit var etPass : EditText
    lateinit var etRePass : EditText
    lateinit var firestore: FirebaseFirestore
    lateinit var auth : FirebaseAuth
    val BuyerDataKey = "BuyerData123Key1212"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_registration)

        etfName = findViewById(R.id.etBuyerFirstName)
        etlName = findViewById(R.id.etBuyerLastName)
        etEmail = findViewById(R.id.etBuyerEmail)
        etPass = findViewById(R.id.etBuyerPass)
        etRePass = findViewById(R.id.etBuyerRePass)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

    }

    fun btnBuyerReg(view: android.view.View) {
        if (etfName.text.toString().isEmpty() || etlName.text.toString().isEmpty() || etEmail.text.toString().isEmpty()
            || etPass.text.toString().isEmpty() || etRePass.text.toString().isEmpty()){
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_LONG).show()
        }else if(!etEmail.text.toString().contains("@")){
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_LONG).show()
        }else if(etPass.text.toString().length < 6){
            Toast.makeText(this, "Password must be at least 6 letters", Toast.LENGTH_LONG).show()
        }else if(!etPass.text.toString().equals(etRePass.text.toString())){
            Toast.makeText(this, "Password do not match", Toast.LENGTH_LONG).show()
        }else{
            uploadData()
            regBuyer()
        }
    }

    private fun uploadData() {
        val tempFullname = etfName.text.toString() +" "+etlName

        val fullName = AESCrypt.encrypt(BuyerDataKey, tempFullname)
        val email = AESCrypt.encrypt(BuyerDataKey, etEmail.text.toString())
        val password = AESCrypt.encrypt(BuyerDataKey, etPass.text.toString())

        val buyer = BuyerModel(fullName, email, password)

        firestore.collection("Buyer").document(etEmail.text.toString()).set(buyer).addOnCompleteListener {

        }
    }

    private fun regBuyer() {
        auth.createUserWithEmailAndPassword(etEmail.text.toString(), etPass.text.toString()).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(this, "Account created", Toast.LENGTH_LONG).show()
                updateUI()
            }else{
                Toast.makeText(this, "Something went wrong! please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI() {
        val i = Intent(this, BuyerHome::class.java)
        startActivity(i)
    }
}