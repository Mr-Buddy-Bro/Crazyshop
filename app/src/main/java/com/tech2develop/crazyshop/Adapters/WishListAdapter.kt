package com.tech2develop.crazyshop.Adapters

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.Models.WishListModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ShopDetailedActivity
import java.io.File
import java.util.ArrayList

class WishListAdapter(context: Context, list: ArrayList<WishListModel>) : RecyclerView.Adapter<WishListAdapter.ViewHolder>() {

    val context = context
    val list = list
    lateinit var loadingDialog : Dialog

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = list[position]

        holder.itemName.text = product.itemName
        holder.itemDesc.text = product.itemDesc
        val price = "Rs. " + product.itemPrice
        val shop = "From : " + product.shopName
        holder.itemPrice.text = price
        holder.shopName.text = shop

        getPrImages(holder, product)

        holder.btnRemoveFromCart.setOnClickListener {
            removeItem(product)
        }

    }

    private fun removeItem(product: WishListModel) {
        var docId : String
        loadingDialog = Dialog(context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.show()
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("Wish list").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        if (doc.data.getValue("itemName").toString() == product.itemName){
                            docId = doc.id

                            firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!)
                                .collection("Wish list").document(docId).delete().addOnCompleteListener {
                                    loadingDialog.dismiss()
                                    Toast.makeText(context, "Item removed from wish list", Toast.LENGTH_LONG).show()
                                }

                            break
                        }
                    }
                    loadingDialog.dismiss()
                }
            }
    }

    private fun getPrImages(holder: ViewHolder, product: WishListModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${product.shopKey}/product images/${product.itemName}.jpg")
        val localImage = File.createTempFile(product.itemName,"jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

            holder.itemImage.setImageBitmap(bitmapImage)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val itemImage = itemView.findViewById<ImageView>(R.id.ivCart)
        val itemName = itemView.findViewById<TextView>(R.id.cartName)
        val itemDesc = itemView.findViewById<TextView>(R.id.cartDesc)
        val itemPrice = itemView.findViewById<TextView>(R.id.cartPrice)
        val shopName = itemView.findViewById<TextView>(R.id.tvCartShopName)
        val itemLayout = itemView.findViewById<ConstraintLayout>(R.id.cartItemLayout)
        val btnRemoveFromCart = itemView.findViewById<ImageView>(R.id.btnRemoveFromCart)

    }

}