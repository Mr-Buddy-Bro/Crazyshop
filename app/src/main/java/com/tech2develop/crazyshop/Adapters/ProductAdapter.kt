package com.tech2develop.crazyshop.Adapters

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.BiMap
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.io.File
import java.util.ArrayList

class ProductAdapter(context: Context, list: ArrayList<ProductModel>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    val context = context
    val list = list
    lateinit var dialog : Dialog
    lateinit var imagBitmap : Bitmap

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.seller_product_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = list[position]

        holder.itemName.text = product.name
        holder.itemCat.text = product.category
        holder.itemPrice.text = "Rs."+product.price
        getPrImages(holder, product)

        holder.editIcon.setOnClickListener {
            dialog = Dialog(context)
            dialog.setContentView(R.layout.add_product_pop_up)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

//            dialog.findViewById<ImageView>(R.id.ivPrAdd).setImageBitmap(imagBitmap)
            dialog.findViewById<EditText>(R.id.etPrName).setText(product.name)
            dialog.findViewById<EditText>(R.id.etPrDesc).setText(product.description)
            dialog.findViewById<EditText>(R.id.etPrPrice).setText(product.price)

            dialog.show()

        }

    }

    private fun getPrImages(holder: ViewHolder, product: ProductModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

            val storageRef = storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg")
            val localImage = File.createTempFile(product.name,"jpg")

            storageRef.getFile(localImage).addOnSuccessListener {
                imagBitmap = BitmapFactory.decodeFile(localImage.absolutePath)

                holder.itemImage.setImageBitmap(imagBitmap)
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
        val editIcon = itemView.findViewById<ImageView>(R.id.btnEditProduct)

    }

}