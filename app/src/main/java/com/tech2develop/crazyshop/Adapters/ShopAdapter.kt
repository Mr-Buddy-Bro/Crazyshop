package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
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

        holder.itemLayout.animation = AnimationUtils.loadAnimation(holder.itemLayout.context, R.anim.rv_layout_anim)
        val shop = list[position]
        pos = position

        holder.name.text = shop.companyName
        holder.desc.text = shop.companyDescription
        holder.name.text = shop.companyName
        Picasso.get().load(shop.bannerUrl).into(holder.shopBanner)

        holder.itemLayout.setOnClickListener {
            val i = Intent(context, ShopDetailedActivity::class.java)
            i.putExtra("shopIndex", position)
            context.startActivity(i)
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
