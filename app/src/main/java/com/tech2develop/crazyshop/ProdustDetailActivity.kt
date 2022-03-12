package com.tech2develop.crazyshop

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ProductModel
import java.io.File

class ProdustDetailActivity : AppCompatActivity() {

    lateinit var product : ProductModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produst_detail)

        val index = intent.getIntExtra("index",0)
        product = ShopDetailedActivity.prodList[index]

        val productImg = findViewById<ImageView>(R.id.ivDetailedProduct)
        val tvName = findViewById<TextView>(R.id.tvDetailedProductName)
        val tvDesc = findViewById<TextView>(R.id.tvDetailedProductDes)
        val tvPrice = findViewById<TextView>(R.id.tvDetailedProductPrice)
        tvName.text = product.name
        tvDesc.text = product.description
        tvPrice.text = "Rs. ${product.price}"

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${ShopDetailedActivity.sellerKey}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name,"jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

            productImg.setImageBitmap(bitmapImage)
        }

    }
}