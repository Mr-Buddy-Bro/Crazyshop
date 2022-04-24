package com.tech2develop.crazyshop

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.Models.SellerModel
import com.tech2develop.crazyshop.Models.SettingsModel
import com.tech2develop.crazyshop.Models.ShopGrphics
import com.tech2develop.crazyshop.databinding.ActivityGraphicsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
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
                    mySeller.password,mySeller.audienceSize,mySeller.isVerified, mySeller.companyName+mySeller.phoneNo.substring(1,5),null,null)
                shopGraphics = ShopGrphics(iconUri,bannerUri)

                regUser()


            }
        }

    }

    private fun regUser() {
        val email = AESCrypt.decrypt(SellerRegistration.eSellerDataKey,seller.email)
        val pass = AESCrypt.decrypt(SellerRegistration.eSellerDataKey,seller.password)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    uploadData()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "createUserWithEmail:success")
                    val user = auth.currentUser
//                    updateUI(user)
                } else {
                    progressDialog.dismiss()
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed. ${task.exception}",
                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }

    private fun uploadData() {
        progressDialog.show()
        try {
            val email = AESCrypt.decrypt(SellerRegistration.eSellerDataKey,seller.email)
            firestore.collection("Seller").document(email).set(seller).addOnCompleteListener {
                if (it.isSuccessful){

                        val setting = SettingsModel(false, "5:00 pm - 6:00 pm", "10",
                            "7:00 am - 4:00 pm")
                    firestore.collection("Seller").document(email).collection("Settings").add(setting)
                }else{
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }

            val storageRef1 = storage.getReference(seller.sellerKey+"/shop graphics/icon.jpg")
            val storageRef2 = storage.getReference(seller.sellerKey+"/shop graphics/banner.jpg")

            storageRef1.putFile(iconUri).addOnCompleteListener{
                if (it.isSuccessful){
                    Log.d("seller123", "uploadData: p")
                    storageRef1.downloadUrl.addOnSuccessListener {task1->
                        Log.d("seller123", "uploadData: pp")
                        val iconUrl = task1.toString()
                        storageRef2.putFile(bannerUri).addOnCompleteListener{task2->
                            Log.d("seller123", "uploadData: ppp")
                            if (task2.isSuccessful){
                                storageRef2.downloadUrl.addOnSuccessListener {task->
                                    Log.d("seller123", "uploadData: pppp")
                                    val bannerUrl = task.toString()
                                    Log.d("seller123", "uploadData: ppppp")
                                    seller = SellerModel(mySeller.companyName,mySeller.companyDescription,mySeller.fullName,mySeller.email,mySeller.phoneNo,
                                        mySeller.password,mySeller.audienceSize,mySeller.isVerified, mySeller.companyName+mySeller.phoneNo.substring(1,5),iconUrl,bannerUrl)
                                    firestore.collection("Seller").document(email).set(seller).addOnSuccessListener {
                                        startActivity(Intent(this,SellerHome::class.java))
                                        Log.d("seller123", "uploadData: ppppp pos")
                                    }
                                }
                            }else{
                                progressDialog.dismiss()
                                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    progressDialog.dismiss()
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }


        }catch (e: Exception){
            progressDialog.dismiss()
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
            val imgUri = data?.data!!
            GlobalScope.launch {
                Log.d("TAG", "onActivityResult: Entered")
                val result = Compress.with(this@GraphicsActivity, imgUri)
                    .setQuality(70)
                    .concrete {
                        withMaxWidth(500f)
                        withMaxHeight(500f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)
                withContext(Dispatchers.Main) {
                    Log.d("TAG", "onActivityResult: ${Formatter.formatShortFileSize(this@GraphicsActivity, result.length())}")
                    iconUri = result.toUri()
                    binding.ivIcon.setImageURI(iconUri)
                    binding.ivIcon.visibility = View.VISIBLE
                    binding.tvIconInfo.visibility = View.INVISIBLE
                    Log.d("TAG", "onActivityResult: "+ data?.data)
                }
            }

        }
        else if(requestCode == BANNER_REQ_CODE && resultCode == RESULT_OK){

            val imgUri = data?.data!!
            GlobalScope.launch {
                Log.d("TAG", "onActivityResult: Entered")
                val result = Compress.with(this@GraphicsActivity, imgUri)
                    .setQuality(70)
                    .concrete {
                        withMaxWidth(500f)
                        withMaxHeight(500f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)
                withContext(Dispatchers.Main) {
                    Log.d("TAG", "onActivityResult: ${Formatter.formatShortFileSize(this@GraphicsActivity, result.length())}")

                    bannerUri = result.toUri()
                    binding.ivBanner.setImageURI(bannerUri)
                    binding.ivBanner.visibility = View.VISIBLE
                    binding.tvBannerInfo.visibility = View.INVISIBLE
                }
            }

        }
        else{
            Log.d("TAG", "onActivityResult: something wrong")
        }
    }

}