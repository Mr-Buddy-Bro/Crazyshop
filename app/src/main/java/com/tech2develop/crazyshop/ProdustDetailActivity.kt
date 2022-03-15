package com.tech2develop.crazyshop

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.Models.WishListModel
import java.io.File

class ProdustDetailActivity : AppCompatActivity() {

    lateinit var product : ProductModel
    var index = 0
    lateinit var prImage : Bitmap
    lateinit var loadingDialog  : Dialog
    lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produst_detail)

        index = intent.getIntExtra("index",0)
        product = ShopDetailedActivity.prodList[index]
        firestore = FirebaseFirestore.getInstance()

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.setCancelable(false)

        val productImg = findViewById<ImageView>(R.id.ivDetailedProduct)
        val tvName = findViewById<TextView>(R.id.tvDetailedProductName)
        val tvDesc = findViewById<TextView>(R.id.tvDetailedProductDes)
        val tvPrice = findViewById<TextView>(R.id.tvDetailedProductPrice)
        tvName.text = product.name
        tvDesc.text = product.description
        tvPrice.text = "Rs. ${product.price}"

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()
        loadingDialog.show()

        val storageRef = storage.getReference().child("${ShopDetailedActivity.sellerKey}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name,"jpg")

        storageRef.getFile(localImage).addOnCompleteListener {
        loadingDialog.dismiss()
            if (it.isSuccessful){
                prImage = BitmapFactory.decodeFile(localImage.absolutePath)

                productImg.setImageBitmap(prImage)
            }
        }
    }

    fun btnContinue(view: View) {
        var name : String? = null
        var addressExist = false
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("Address").get().addOnCompleteListener{
            if (it.isSuccessful){
                for (doc in it.result!!){
                        try {
                            name = doc.data.getValue("name").toString()
                        }catch (e: Exception){
                            Log.d("TAG", "btnContinue: No address found")
                        }
                        if (name != null){
                            addressExist = true
                        }
                }
                goTo(addressExist)
            }
        }
    }

    private fun goTo(addressExist: Boolean) {
        if (addressExist) {
            val intent = Intent(this, CheckOutActivity::class.java)
            intent.putExtra("index", index)
            startActivity(intent)
        }else{
            val intent = Intent(this, CreateAddressActivity::class.java)
            intent.putExtra("index", index)
            startActivity(intent)
        }
    }

    fun btnAddToWishList(view: View) {
        loadingDialog.show()
        val wishItem = WishListModel(product.name!!, product.description!!, product.price!!, ShopDetailedActivity.shop.companyName!!, ShopDetailedActivity.sellerKey)

        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("Wish list").add(wishItem)
            .addOnCompleteListener {
                loadingDialog.dismiss()
                if (it.isSuccessful){
                    Toast.makeText(this, "Added to Wish list", Toast.LENGTH_LONG).show()
                }
            }
    }
}