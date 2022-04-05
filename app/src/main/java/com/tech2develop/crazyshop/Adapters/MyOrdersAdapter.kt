package com.tech2develop.crazyshop.Adapters

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.util.Log
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
import com.tech2develop.crazyshop.Models.AddressModel
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ui.buyerFragments.BuyerMyOrdersFragment
import java.io.File

class MyOrdersAdapter(context : Context, arrayList: ArrayList<OrderModel>) : RecyclerView.Adapter<MyOrdersAdapter.ViewHolder>() {

    val myContext = context
    val list = arrayList
    lateinit var loadingDialog : Dialog

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

        loadingDialog = Dialog(myContext)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        val item = list[position]
        holder.tvMyOrderName.text = item.itemName
        holder.tvMyOrderDate.text = item.date
        holder.tvMyOrderPrice.text = "Rs. "+item.itemPrice
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
        loadingDialog.show()
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("All orders").document(item.docId!!).delete()
            .addOnCompleteListener {

            }

        firestore.collection("Seller").get()
            .addOnCompleteListener {

                val docId : String
                if (it.isSuccessful){
                    for(doc in it.result!!){
//                        loadingDialog.dismiss()
                        if (doc.data.getValue("sellerKey").toString() == item.shopKey){
                            docId = doc.id

                            firestore.collection("Seller").document(docId).collection("All orders").get()
                                .addOnCompleteListener { t1->
                                    loadingDialog.dismiss()
                                    if (t1.isSuccessful){
//                                        Log.d("deleteorder", "deleteItem: yeeeeeah")
                                        val sDocId : String
                                        for (mDoc in t1.result!!){
                                            Log.d("deleteorder", "deleteItem: yeeeeeah")
                                            val myAddress = mDoc.data.getValue("deliveryAddress") as Map<*,*>
//
                                            val address = AddressModel(myAddress["name"].toString(), myAddress["houseNo"].toString(), myAddress["houseName"].toString(),
                                                myAddress["landmark"].toString(), myAddress["phoneNo"].toString())
                                            Log.d("deleteorder", "deleteItem: ${address.phoneNo} ${item.deliveryAddress!!.phoneNo}")
                                            if (mDoc.data.getValue("itemName").toString() == item.itemName && address.phoneNo == item.deliveryAddress!!.phoneNo){
                                                Log.d("deleteorder", "deleteItem: mmmmmmm")
                                                sDocId = mDoc.id
                                                Log.d("deleteorder", sDocId)
                                                firestore.collection("Seller").document(docId).collection("All orders")
                                                    .document(sDocId).delete().addOnCompleteListener{
                                                        if (it.isSuccessful){
                                                            Toast.makeText(myContext, "Order cancelled", Toast.LENGTH_SHORT).show()
                                                        }else{
                                                            Log.d("deleteorder", it.exception.toString())
                                                        }

                                                    }
                                                break
                                            }
                                        }
                                    }else{
                                        Toast.makeText(myContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                                        Log.d("deleteorder", "deleteItem: Noooo")
                                    }
                                }

                            break
                        }
                    }
                }else{
                    Toast.makeText(myContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
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