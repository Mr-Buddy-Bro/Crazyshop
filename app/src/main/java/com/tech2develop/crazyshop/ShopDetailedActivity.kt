package com.tech2develop.crazyshop

import android.app.Dialog
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import com.tech2develop.crazyshop.Adapters.ShopProductsAdapter
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.Models.ShopModel
import com.tech2develop.crazyshop.ui.buyerFragments.BuyerHomeFragment
import java.io.File

class ShopDetailedActivity : AppCompatActivity() {

    var shopIndex : Int = 0

    lateinit var ivIcon : RoundedImageView
    lateinit var ivBanner : ImageView
    lateinit var tvShopName : TextView
    lateinit var tvFullName : TextView
    lateinit var tvDesc : TextView
    lateinit var sp_cat : Spinner
    lateinit var catList : ArrayList<String>
    lateinit var firestore : FirebaseFirestore
    lateinit var selectedCat : String
    lateinit var loadingDialog  : Dialog
    lateinit var etSearch : EditText
    companion object{
        lateinit var shop : ShopModel
        lateinit var sellerKey: String
        lateinit var prodList : ArrayList<ProductModel>
        lateinit var searchProdList : ArrayList<ProductModel>
        var deliveryCharge = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_detailed)

        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        loadingDialog.setCancelable(false)

        shopIndex = intent.getIntExtra("shopIndex", 0)
        shop = BuyerHomeFragment.shopsList[shopIndex]
        sellerKey = shop.sellerKey!!
        searchProdList = ArrayList()

        ivIcon = findViewById(R.id.iv_shop_detailed_icon)
        tvShopName = findViewById(R.id.tv_shop_detailed_name)
        tvFullName = findViewById(R.id.tv_shop_detailed_fullName)
        tvDesc = findViewById(R.id.tv_shop_detailed_desc)
        ivBanner = findViewById(R.id.iv_shop_banner)
        sp_cat = findViewById(R.id.sp_shop_cat)
        etSearch = findViewById(R.id.etSearch)
        etSearch.doOnTextChanged { text, start, before, count ->
            searchProdList.clear()
            for (doc in prodList){
                if (doc.name!!.contains(text.toString()) || doc.description!!.contains(text.toString())){
                    searchProdList.add(doc)
                }
            }
            setAdapter(searchProdList)
        }

        catList = ArrayList()
        prodList = ArrayList()

        firestore = FirebaseFirestore.getInstance()

        Picasso.get().load(shop.iconUrl).into(ivIcon)
        Picasso.get().load(shop.bannerUrl).into(ivBanner)

        tvShopName.text = shop.companyName
        tvFullName.text = shop.fullName
        tvDesc.text = shop.companyDescription
        getCat()
    }

    private fun getProducts() {
        selectedCat = sp_cat.selectedItem.toString()
        firestore.collection("Seller").document(shop.email!!).collection("Products").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        if(selectedCat.equals("All")){
                            val prodItem = ProductModel(
                                doc.data.getValue("name").toString(),
                                doc.data.getValue("description").toString(),
                                doc.data.getValue("category").toString(),
                                doc.data.getValue("price").toString(), null,
                                doc.data.getValue("imageUrl").toString(),
                                doc.data.getValue("inStock") as Boolean
                            )
                            prodList.add(prodItem)
                        }else if (doc.data.getValue("category").toString().equals(selectedCat)) {
                            val prodItem = ProductModel(
                                doc.data.getValue("name").toString(),
                                doc.data.getValue("description").toString(),
                                doc.data.getValue("category").toString(),
                                doc.data.getValue("price").toString(), null,
                                doc.data.getValue("imageUrl").toString(),
                                doc.data.getValue("inStock") as Boolean
                            )
                            prodList.add(prodItem)
                        }
                    }
                    setAdapter(prodList)
                }
            }
    }

    private fun setAdapter(list: ArrayList<ProductModel>) {
        loadingDialog.dismiss()
        val adapter = ShopProductsAdapter(this,list)
        val rv = findViewById<RecyclerView>(R.id.rvShopProducts)
        rv.isNestedScrollingEnabled = true
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

    }

    private fun getCat() {
        loadingDialog.show()
        catList.add("All")
        firestore.collection("Seller").document(
            shop.email!!
        ).collection("Categories").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    val catItem = doc.data.getValue("name").toString()
                    catList.add(catItem)
                }
                setCategory()
            }
        }

        // getting delivery charge per item
        firestore.collection("Seller").document(
            shop.email!!
        ).collection("Settings").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    deliveryCharge = doc.data.getValue("deliveryCharge").toString().toInt()
                }
            }
        }
    }

    private fun setCategory() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, catList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_cat.adapter = adapter
        getProducts()
    }

    fun btnApplyFilter(view: android.view.View) {
        prodList.clear()
        getProducts()
    }
}

