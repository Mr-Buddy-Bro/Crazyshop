package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.Models.AddressModel

class CreateAddressActivity : AppCompatActivity() {

    lateinit var fName : EditText
    lateinit var lName : EditText
    lateinit var mobile : EditText
    lateinit var hName : EditText
    lateinit var hNumber : EditText
    lateinit var landmark : EditText
    lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_address)

        fName = findViewById(R.id.etAdFName)
        lName = findViewById(R.id.etAdLName)
        mobile = findViewById(R.id.etMobile)
        hName = findViewById(R.id.etHouseName)
        hNumber = findViewById(R.id.etHouseNumber)
        landmark = findViewById(R.id.etLandmark)

        firestore = FirebaseFirestore.getInstance()

    }

    fun btnSaveAddress(view: View) {
        if(fName.text.isEmpty() || lName.text.isEmpty() || mobile.text.isEmpty() || hName.text.isEmpty() || hNumber.text.isEmpty() ||
            landmark.text.isEmpty()){
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
        }else{
            val name = fName.text.toString() + " "+lName.text.toString()
            val address = AddressModel(name, hNumber.text.toString(), hName.text.toString(), landmark.text.toString(), mobile.text.toString())
            firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("Address").add(address).addOnCompleteListener {
                if (it.isSuccessful){
                    val index = intent.getIntExtra("index", 0)
                    val intent = Intent(this, CheckOutActivity::class.java)
                    intent.putExtra("index", index)
                    startActivity(intent)
                }
            }
        }
    }
}