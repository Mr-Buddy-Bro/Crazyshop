package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.Models.SellerModel
import com.tech2develop.crazyshop.databinding.ActivitySellerRegistrationBinding

class SellerRegistration : AppCompatActivity() {

    lateinit var binding : ActivitySellerRegistrationBinding

    companion object{
        lateinit var seller : SellerModel
        val eSellerDataKey = "sellerDataKey1332"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun btnNext1(view: android.view.View) {
        if (binding.etCompanyName.text.toString().isEmpty()||binding.etCompanyDescription.text.toString().isEmpty()||binding.etFullName.text.toString().isEmpty()
            ||binding.etEmail.text.toString().isEmpty()||binding.etPhone.text.toString().isEmpty()||binding.etPass.text.toString().isEmpty()||
            binding.etRePass.text.toString().isEmpty()){
            Toast.makeText(this, "Please fill all the details above", Toast.LENGTH_LONG).show()
        }else if (!binding.etEmail.text!!.contains("@")){

            Toast.makeText(this, "Invalid email", Toast.LENGTH_LONG).show()

        }else if (binding.etPass.text!!.length < 6){
            Toast.makeText(this, "password must be at least 6 letters", Toast.LENGTH_LONG).show()
        }else if (!binding.etPass.text.toString().equals(binding.etRePass.text.toString())){
            Toast.makeText(this, "Password do not match", Toast.LENGTH_LONG).show()
        }else{
            val companyName = AESCrypt.encrypt(eSellerDataKey,binding.etCompanyName.text.toString())
            val companyDesc = AESCrypt.encrypt(eSellerDataKey,binding.etCompanyDescription.text.toString())
            val fullName = AESCrypt.encrypt(eSellerDataKey,binding.etFullName.text.toString())
            val email = AESCrypt.encrypt(eSellerDataKey,binding.etEmail.text.toString())
            val phone = AESCrypt.encrypt(eSellerDataKey,binding.etPhone.text.toString())
            val password = AESCrypt.encrypt(eSellerDataKey,binding.etPass.text.toString())

            seller = SellerModel(companyName,companyDesc,fullName,email,phone,password,null,"false",null)
            nextAct()
        }
    }

    fun nextAct(){
        val i = Intent(this,ChooseAudience::class.java)
        startActivity(i)
    }
}