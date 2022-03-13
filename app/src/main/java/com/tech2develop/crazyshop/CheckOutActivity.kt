package com.tech2develop.crazyshop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ProductModel
import java.io.File

class CheckOutActivity : AppCompatActivity() {

    lateinit var product : ProductModel
    var index = 0
    lateinit var ivProd : ImageView
    lateinit var tvName : TextView
    lateinit var tvDesc : TextView
    lateinit var tvItemPrice : TextView
    lateinit var tvDeliveryCharge : TextView
    lateinit var tvTotalPrice : TextView
    var deliveryCharge = 10
    lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        index = intent.getIntExtra("index",0)
        product = ShopDetailedActivity.prodList[index]

        tvName = findViewById(R.id.tvCheckName)
        tvDesc = findViewById(R.id.tvDescCheck)
        tvItemPrice = findViewById(R.id.tvCheckPrice)
        tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge)
        tvTotalPrice = findViewById(R.id.tvTotalAmount)
        ivProd = findViewById(R.id.ivCheckout)

        tvName.text = product.name
        tvDesc.text = product.description
        tvItemPrice.text = "Rs. ${product.price}"
        tvDeliveryCharge.text = "Rs. $deliveryCharge"
        val tPrice = "Rs. ${(product.price!!.toInt() + deliveryCharge)}"
        tvTotalPrice.text = tPrice

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${ShopDetailedActivity.sellerKey}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name,"jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val prImage = BitmapFactory.decodeFile(localImage.absolutePath)

            ivProd.setImageBitmap(prImage)
        }

    }

    fun btnPlaceOrder(view: View) {

    }
}