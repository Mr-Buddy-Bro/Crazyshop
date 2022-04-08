package com.tech2develop.crazyshop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tech2develop.crazyshop.Models.SellerModel
import com.tech2develop.crazyshop.databinding.ActivityChooseAudienceBinding

class ChooseAudience : AppCompatActivity() {

    lateinit var binding : ActivityChooseAudienceBinding
    lateinit var mySeller : SellerModel
    companion object{
        lateinit var seller : SellerModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseAudienceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySeller = SellerRegistration.seller

        binding.btnNextAudience.setOnClickListener {
            if (binding.radio1.isChecked) {
                seller = SellerModel(mySeller.companyName,mySeller.companyDescription,mySeller.fullName,mySeller.email,mySeller.phoneNo,
                    mySeller.password,"small",mySeller.isVerified,null, null, null)
                nextAct()
            }
            else if (binding.radio2.isChecked){
                seller = SellerModel(mySeller.companyName,mySeller.companyDescription,mySeller.fullName,mySeller.email,mySeller.phoneNo,
                    mySeller.password,"standard",mySeller.isVerified,null, null, null)
                nextAct()
            }
            else if(binding.radio3.isChecked){
                seller = SellerModel(mySeller.companyName,mySeller.companyDescription,mySeller.fullName,mySeller.email,mySeller.phoneNo,
                    mySeller.password,"large",mySeller.isVerified,null, null, null)
                nextAct()
            }
            else{
                Toast.makeText(this, "Please choose an audience", Toast.LENGTH_LONG).show()
            }
        }

    }

    fun nextAct() {

        val i = Intent(this, GraphicsActivity::class.java)
        startActivity(i)
    }
}