package com.tech2develop.crazyshop.Adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.tech2develop.crazyshop.Models.AddressModel
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ShopDetailedActivity
import java.io.File
import java.util.ArrayList

class AllOrdersAdapter(context: Context, list: ArrayList<OrderModel>) : RecyclerView.Adapter<AllOrdersAdapter.ViewHolder>() {

    val context = context
    val list = list
    lateinit var orderDetailedDialog : Dialog

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.all_orders_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        orderDetailedDialog = Dialog(context)
        orderDetailedDialog.setContentView(R.layout.order_det_dialog_lay)
        orderDetailedDialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)
        orderDetailedDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        orderDetailedDialog.window!!.setGravity(Gravity.BOTTOM)

        val item = list[position]
        holder.itemName.text = item.itemName
        holder.itemPrice.text = item.itemPrice
        holder.orderStatus.text = item.deliveryStatus

        getPrImages(holder, item)

        holder.orderLayout.setOnClickListener {

            val iv_order_det_dialog = orderDetailedDialog.findViewById<RoundedImageView>(R.id.iv_order_det_dialog)
            val tvDelItemName = orderDetailedDialog.findViewById<TextView>(R.id.textView84)
            val tvDelItemPrice = orderDetailedDialog.findViewById<TextView>(R.id.textView88)
            val tvDelItemAddress = orderDetailedDialog.findViewById<TextView>(R.id.textView86)

            val storage : FirebaseStorage
            storage = FirebaseStorage.getInstance()

            val storageRef = storage.getReference().child("${SellerHome.shopId}/product images/${item.itemName}.jpg")
            val localImage = File.createTempFile(item.itemName,"jpg")

            storageRef.getFile(localImage).addOnSuccessListener {
                val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

               iv_order_det_dialog.setImageBitmap(bitmapImage)
            }

            tvDelItemName.text = item.itemName
            tvDelItemPrice.text = item.itemPrice
            val address = item.deliveryAddress!!
            val mAddress = "${address.name} \n ${address.houseNo} \n ${address.houseName} \n ${address.landmark} \n ${address.phoneNo}"
            tvDelItemAddress.text = mAddress

            orderDetailedDialog.show()
        }

        val btnMarkDelivered = orderDetailedDialog.findViewById<MaterialButton>(R.id.btnMarkAsDelivered)
        if (item.deliveryStatus == "Un-delivered"){
            btnMarkDelivered.setText(R.string.mark_as_delivered)
        }else{
            btnMarkDelivered.setText(R.string.mark_as_un_delivered)
        }

        btnMarkDelivered.setOnClickListener {

            val firestore = FirebaseFirestore.getInstance()
            var docId : String

            if (item.deliveryStatus == "Un-delivered"){

                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                    .collection("All orders").get().addOnCompleteListener {
                        if (it.isSuccessful){
                            for (doc in it.result!!){
                                if (doc.data.getValue("itemName").toString() == item.itemName){
                                    docId = doc.id

                                    firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                                        .collection("All orders").document(docId).update("deliveryStatus", "Delivered").addOnCompleteListener {
                                            btnMarkDelivered.setText(R.string.mark_as_un_delivered)
                                            orderDetailedDialog.dismiss()
                                        }

                                    break
                                }
                            }

                        }
                    }
            }else{
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                    .collection("All orders").get().addOnCompleteListener {
                        if (it.isSuccessful){
                            for (doc in it.result!!){
                                if (doc.data.getValue("itemName").toString() == item.itemName){
                                    docId = doc.id

                                    firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                                        .collection("All orders").document(docId).update("deliveryStatus", "Un-delivered").addOnCompleteListener {
                                            btnMarkDelivered.setText(R.string.mark_as_delivered)
                                            orderDetailedDialog.dismiss()
                                        }

                                    break
                                }
                            }

                        }
                    }
            }
        }

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
        val orderLayout = itemView.findViewById<ConstraintLayout>(R.id.orderLayout)

    }

}