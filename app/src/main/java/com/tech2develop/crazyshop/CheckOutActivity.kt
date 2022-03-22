package com.tech2develop.crazyshop

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.AddressModel
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.Models.ProductModel
import java.io.File
import java.util.*

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
    lateinit var orderPlacedDialog : Dialog
    lateinit var loadingDialog  : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        index = intent.getIntExtra("index",0)
        product = ShopDetailedActivity.prodList[index]

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.setCancelable(false)

        firestore = FirebaseFirestore.getInstance()

        tvName = findViewById(R.id.tvCheckName)
        tvDesc = findViewById(R.id.tvDescCheck)
        tvItemPrice = findViewById(R.id.tvCheckPrice)
        tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge)
        tvTotalPrice = findViewById(R.id.tvTotalAmount)
        ivProd = findViewById(R.id.ivCheckout)

        orderPlacedDialog = Dialog(this)
        orderPlacedDialog.setContentView(R.layout.order_placed_layout)

        tvName.text = product.name
        tvDesc.text = product.description
        tvItemPrice.text = "Rs. ${product.price}"
        tvDeliveryCharge.text = "Rs. $deliveryCharge"
        val tPrice = "Rs. ${(product.price!!.toInt() + deliveryCharge)}"
        tvTotalPrice.text = tPrice

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()
        loadingDialog.show()
        val storageRef = storage.getReference().child("${ShopDetailedActivity.sellerKey}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name,"jpg")

        storageRef.getFile(localImage).addOnCompleteListener{
            loadingDialog.dismiss()
            if (it.isSuccessful) {
                val prImage = BitmapFactory.decodeFile(localImage.absolutePath)
                ivProd.setImageBitmap(prImage)
            }
        }

    }

    fun btnPlaceOrder(view: View) {
        loadingDialog.show()
        var address = AddressModel(null, null, null, null, null)
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("Address").get().addOnCompleteListener{
            if (it.isSuccessful){
                for (doc in it.result!!){

                      address = AddressModel(doc.data.getValue("name").toString(),
                          doc.data.getValue("houseNo").toString(),
                          doc.data.getValue("houseName").toString(),
                          doc.data.getValue("landmark").toString(),
                          doc.data.getValue("phoneNo").toString())

                }
                placeOrder(address)
            }
        }


    }

    private fun placeOrder(address: AddressModel) {

        val calendar = Calendar.getInstance().time
        val date = calendar.date.toString() +"/"+(calendar.month+1).toString()+"/"+calendar.year.toString()
        val orderItem = OrderModel(product.name, address, (product.price!!.toInt() + deliveryCharge).toString(), "Un-delivered", ShopDetailedActivity.shop.companyName,date, ShopDetailedActivity.sellerKey, null)

        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("All orders")
            .add(orderItem).addOnCompleteListener {
                loadingDialog.dismiss()
                if (it.isSuccessful){
                    orderPlacedDialog.show()
                }
            }

        firestore.collection("Seller").document(ShopDetailedActivity.shop.email!!).collection("All orders")
            .add(orderItem).addOnCompleteListener {
                loadingDialog.dismiss()
                if (it.isSuccessful){
                    orderPlacedDialog.show()
                }
            }

    }

    fun btnGoToMyOrders(view: View) {
        val i = Intent(this,BuyerHome::class.java)
        i.putExtra("btnType", "myOrders")
        startActivity(i)
        finish()
    }
    fun btnShopMore(view: View) {
        val i = Intent(this,BuyerHome::class.java)
        startActivity(i)
        finish()
    }
}