package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ui.buyerFragments.BuyerMyOrdersFragment
import java.io.File

class MyOrdersAdapter(context : Context, arrayList: ArrayList<OrderModel>) : RecyclerView.Adapter<MyOrdersAdapter.ViewHolder>() {

    val myContext = context
    val list = arrayList

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val ivMyOrders = itemView.findViewById<ImageView>(R.id.ivMyOrders)
        val tvMyOrderName = itemView.findViewById<TextView>(R.id.tvMyOrderName)
        val tvMyOrderDate = itemView.findViewById<TextView>(R.id.tvMyOrderDate)
        val tvMyOrderPrice = itemView.findViewById<TextView>(R.id.tvMyOrderPrice)
        val tvDelivered = itemView.findViewById<TextView>(R.id.tvDelivered)
        val btnRemoveMyOrder = itemView.findViewById<MaterialButton>(R.id.btnRemoveMyOrder)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersAdapter.ViewHolder {
        val view = LayoutInflater.from(myContext).inflate(R.layout.buyer_my_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOrdersAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.tvMyOrderName.text = item.itemName
        holder.tvMyOrderDate.text = item.date
        holder.tvMyOrderPrice.text = item.itemPrice
        getPrImages(holder, item)
        if (item.deliveryStatus.equals("Delivered")){
            holder.btnRemoveMyOrder.visibility = View.INVISIBLE
            holder.tvDelivered.visibility = View.VISIBLE
        }
        holder.btnRemoveMyOrder.setOnClickListener {
            deleteItem(item)
        }
    }

    private fun deleteItem(item: OrderModel) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("All orders").document(item.docId!!).delete()
            .addOnCompleteListener {
                Toast.makeText(myContext, "Order cancelled", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getPrImages(holder: ViewHolder, product: OrderModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${product.shopKey}/product images/${product.itemName}.jpg")
        val localImage = File.createTempFile(product.itemName,"jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

            holder.ivMyOrders.setImageBitmap(bitmapImage)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}