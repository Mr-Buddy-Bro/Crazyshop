package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ShopDetailedActivity
import java.io.File
import java.util.ArrayList

class AllOrdersAdapter(context: Context, list: ArrayList<OrderModel>) : RecyclerView.Adapter<AllOrdersAdapter.ViewHolder>() {

    val context = context
    val list = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.all_orders_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        holder.itemName.text = item.itemName
        holder.itemPrice.text = item.itemPrice
        holder.orderStatus.text = item.deliveryStatus

        getPrImages(holder, item)

    }

    private fun getPrImages(holder: ViewHolder, product: OrderModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${SellerHome.shopId}/product images/${product.itemName}.jpg")
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

        val itemName = itemView.findViewById<TextView>(R.id.tvOrderName)
        val itemPrice = itemView.findViewById<TextView>(R.id.tvOrderPrice)
        val orderStatus = itemView.findViewById<TextView>(R.id.tvOrderStatus)
        val itemImage = itemView.findViewById<ImageView>(R.id.ivOrderImage)

    }

}