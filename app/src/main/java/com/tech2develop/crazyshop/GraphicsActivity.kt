package com.tech2develop.crazyshop

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.SellerModel
import com.tech2develop.crazyshop.Models.ShopGrphics
import com.tech2develop.crazyshop.databinding.ActivityGraphicsBinding
import java.lang.Exception

class GraphicsActivity : AppCompatActivity() {

    val ICON_REQ_CODE = 100
    val BANNER_REQ_CODE = 101

    lateinit var binding : ActivityGraphicsBinding
    lateinit var iconUri : Uri
    lateinit var bannerUri : Uri

    lateinit var mySeller : SellerModel
    companion object{
        lateinit var seller : SellerModel
    }
    lateinit var shopGraphics : ShopGrphics

    lateinit var firestore : FirebaseFirestore
    lateinit var storage: FirebaseStorage
    lateinit var auth : FirebaseAuth
    lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mySeller = ChooseAudience.seller
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Creating seller")

        binding.btnSubmitRegistration.setOnClickListener {
            if (binding.tvIconInfo.visibility == View.VISIBLE){
                Toast.makeText(this, "Please choose a Shop Icon",Toast.LENGTH_LONG).show()
            }
            else if(binding.tvBannerInfo.visibility == View.VISIBLE){
                Toast.makeText(this, "Please choose a Shop Banner",Toast.LENGTH_LONG).show()
            }
            else{
                // upload data and register

                seller = SellerModel(mySeller.companyName,mySeller.companyDescription,mySeller.fullName,mySeller.email,mySeller.phoneNo,
                    mySeller.password,mySeller.audienceSize,mySeller.isVerified, mySeller.companyName+mySeller.phoneNo.substring(1,8))
                shopGraphics = ShopGrphics(iconUri,bannerUri)

                uploadData()


            }
        }

    }

    private fun regUser() {
        auth.createUserWithEmailAndPassword(seller.email, seller.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success")
                    val user = auth.currentUser
//                    updateUI(user)
                    startActivity(Intent(this,SellerHome::class.java))
                } else {
                    progressDialog.dismiss()
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }

    private fun uploadData() {
        progressDialog.show()
        try {
            firestore.collection("Seller").document(seller.email).set(seller).addOnCompleteListener {
                if (it.isSuccessful){
                    //here
                }else{
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }

            val storageRef1 = storage.getReference(seller.sellerKey+"/shop graphics/icon.jpg")
            val storageRef2 = storage.getReference(seller.sellerKey+"/shop graphics/banner.jpg")
            storageRef1.putFile(iconUri).addOnCompleteListener{
                if (it.isSuccessful){
                    //code
                }else{
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
            storageRef2.putFile(bannerUri).addOnCompleteListener{
                if (it.isSuccessful){
                    regUser()
                    //code
                }else{
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }

        }catch (e: Exception){
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }

    }

    fun btnChooseIcon(view: android.view.View) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = "image/*"
        startActivityForResult(i, ICON_REQ_CODE)
    }

    fun btnChooseBanner(view: android.view.View) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = "image/*"
        startActivityForResult(i, BANNER_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ICON_REQ_CODE && resultCode == RESULT_OK){
            iconUri = data?.data as Uri
            binding.ivIcon.setImageURI(iconUri)
            binding.ivIcon.visibility = View.VISIBLE
            binding.tvIconInfo.visibility = View.INVISIBLE
            Log.d("TAG", "onActivityResult: "+ data?.data)
        }
        else if(requestCode == BANNER_REQ_CODE && resultCode == RESULT_OK){
            bannerUri = data?.data as Uri
            binding.ivBanner.setImageURI(bannerUri)
            binding.ivBanner.visibility = View.VISIBLE
            binding.tvBannerInfo.visibility = View.INVISIBLE
        }
        else{
            Log.d("TAG", "onActivityResult: something wrong")
        }
    }

}