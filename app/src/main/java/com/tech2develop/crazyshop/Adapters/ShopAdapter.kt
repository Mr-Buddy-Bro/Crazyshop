package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ShopModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.ShopDetailedActivity
import java.io.File
import java.util.ArrayList

class ShopAdapter(context: Context, list: ArrayList<ShopModel>) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    val context = context
    val list = list
    var pos = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View
        if (pos%2 == 0) {
            view =
                LayoutInflater.from(context).inflate(R.layout.shop_item_layout1, parent, false)
        }else{
            view =
                LayoutInflater.from(context).inflate(R.layout.shop_item_layout2, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val shop = list[position]
        pos = position

        holder.name.text = shop.companyName
        holder.desc.text = shop.companyDescription
        holder.name.text = shop.companyName

        getShopImages(holder, shop)
        holder.itemLayout.setOnClickListener {
            val i = Intent(context, ShopDetailedActivity::class.java)
            i.putExtra("shopIndex", position)
            context.startActivity(i)
        }

    }

    private fun getShopImages(holder: ViewHolder, shop: ShopModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${shop.sellerKey}/shop graphics/icon.jpg")
        val localImage = File.createTempFile("shopIcon","jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

            holder.shopBanner.setImageBitmap(bitmapImage)
        }.addOnFailureListener{
            Toast.makeText(context, shop.sellerKey, Toast.LENGTH_LONG).show()
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val shopBanner = itemView.findViewById<ImageView>(R.id.ivShopItemImage)
        val name = itemView.findViewById<TextView>(R.id.tvShopItemName)
        val desc = itemView.findViewById<TextView>(R.id.tvShopItemDescription)
        val itemLayout = itemView.findViewById<CardView>(R.id.shopItemLay)

    }

}
