package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.io.File
import java.util.ArrayList

class ProductAdapter(context: Context, list: ArrayList<ProductModel>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    val context = context
    val list = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.product_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = list[position]

        holder.itemName.text = product.name
        holder.itemCat.text = product.category
        holder.itemPrice.text = "Rs."+product.price
        getPrImages(holder, product)

//        Log.d("TAG", "onBindViewHolder: ${ivList[position]}")
//        holder.itemImage.setImageBitmap(ivList[position])

//        holder.addressLayout.setOnClickListener {
//            var addressId = addressItem
//            val i = Intent(context, CheckoutActivity::class.java)
//            i.putExtra("id", item.itemId)
//            i.putExtra("cat", item.cat)
//            i.putExtra("amount", item.amount)
//            i.putExtra("addressId",myAddressId)
//            context.startActivity(i)
//        }
    }

    private fun getPrImages(holder: ViewHolder, product: ProductModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

            val storageRef = storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg")
            val localImage = File.createTempFile(product.name,"jpg")

            storageRef.getFile(localImage).addOnSuccessListener {
                val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

                holder.itemImage.setImageBitmap(bitmapImage)
            }
        }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val itemImage = itemView.findViewById<ImageView>(R.id.imageView2)
        val itemName = itemView.findViewById<TextView>(R.id.textView30)
        val itemCat = itemView.findViewById<TextView>(R.id.textView31)
        val itemPrice = itemView.findViewById<TextView>(R.id.textView33)
        val editIcon = itemView.findViewById<ImageView>(R.id.imageView3)

    }

}