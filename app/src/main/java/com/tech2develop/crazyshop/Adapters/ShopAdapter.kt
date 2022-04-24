package com.tech2develop.crazyshop.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tech2develop.crazyshop.Models.ShopModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.ShopDetailedActivity
import java.util.ArrayList

class ShopAdapter(context: Context, list: ArrayList<ShopModel>) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    val context = context
    val list = list
    var pos = 1
    lateinit var firestore : FirebaseFirestore

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

        firestore = FirebaseFirestore.getInstance()

        holder.itemLayout.animation = AnimationUtils.loadAnimation(holder.itemLayout.context, R.anim.rv_layout_anim)
        val shop = list[position]
        pos = position

        holder.name.text = shop.companyName
        holder.desc.text = shop.companyDescription
        holder.name.text = shop.companyName
        Picasso.get().load(shop.bannerUrl).into(holder.shopBanner)

        getShopStatus(holder, shop.sellerKey)

        holder.itemLayout.setOnClickListener {
            val i = Intent(context, ShopDetailedActivity::class.java)
            i.putExtra("shopIndex", position)
            context.startActivity(i)
        }
    }

    private fun getShopStatus(holder: ViewHolder, sellerKey: String?) {
        var shopId = ""
        firestore.collection("Seller").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (doc in it.result!!) {
                        if (sellerKey == doc.data.getValue("sellerKey").toString()) {
                            shopId = doc.id
                            getStatus(holder, shopId)
                            break
                        }
                    }
                }
            }
    }

    private fun getStatus(holder: ViewHolder, shopId: String) {
        var shopActive = false
        var deliveryTime = ""

        firestore.collection("Seller").document(shopId).collection("Settings").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        shopActive = doc.data.getValue("active") as Boolean
                        deliveryTime = doc.data.getValue("deliveryTime").toString()
                    }
                    if (shopActive){
                        holder.tvShopStatus.text = "Active"
                        holder.ivStatus.setColorFilter(Color.argb(255,0,255,0))
                    }else{
                        holder.tvShopStatus.text = "In-active"
                        holder.ivStatus.setColorFilter(Color.argb(255,255,0,0))
                    }
                    holder.tvDeliveryTime.text = "Delivery at : $deliveryTime"
                }
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
        val tvShopStatus = itemView.findViewById<TextView>(R.id.tvShopStatus)
        val tvDeliveryTime = itemView.findViewById<TextView>(R.id.tvDeliveryTime)
        val ivStatus = itemView.findViewById<ImageView>(R.id.ivStatus)

    }

}
