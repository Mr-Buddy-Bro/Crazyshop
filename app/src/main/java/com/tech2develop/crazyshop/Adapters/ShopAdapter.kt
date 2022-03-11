package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.ShopModel
import com.tech2develop.crazyshop.R
import java.io.File
import java.util.ArrayList

class ShopAdapter(context: Context, list: ArrayList<ShopModel>) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    val context = context
    val list = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.shop_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val shop = list[position]

        holder.name.text = shop.companyName
        holder.desc.text = shop.companyDescription
        holder.name.text = shop.companyName

        getPrImages(holder, shop)

    }

    private fun getPrImages(holder: ViewHolder, shop: ShopModel) {

        val storage : FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${shop.sellerKey}/shop graphics/icon.jpg")
        val localImage = File.createTempFile("shopIcon","jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

            holder.shopBanner.setImageBitmap(bitmapImage)
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