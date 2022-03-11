package com.tech2develop.crazyshop.ui.buyerFragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.Adapters.ShopAdapter
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.Models.CategoryModel
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.Models.ShopModel
import com.tech2develop.crazyshop.Models.ShopsDocIdModel
import com.tech2develop.crazyshop.R

class BuyerHomeFragment : Fragment(R.layout.fragment_buyer_home) {

    lateinit var firestore : FirebaseFirestore
    lateinit var dialog: Dialog
    lateinit var mySellerDocId : String
    lateinit var shopsList : ArrayList<ShopModel>
    lateinit var catList : ArrayList<CategoryModel>
    lateinit var prodList : ArrayList<ProductModel>
    lateinit var sellerDoc : String
    lateinit var shopDocList : ArrayList<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = true

        firestore = FirebaseFirestore.getInstance()
        catList = ArrayList()
        prodList = ArrayList()
        shopDocList = ArrayList()
        shopsList = ArrayList()

        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.add_shop_dialog)

        getAllShops(view)

        view.findViewById<MaterialButton>(R.id.btnAddShop).setOnClickListener {
            dialog.show()
        }
        dialog.findViewById<MaterialButton>(R.id.btnAddShopFirm).setOnClickListener {
            getShop(view)
        }

    }

    private fun getShop(view: View) {
        val myShopId = dialog.findViewById<EditText>(R.id.editText2).text.toString()
        if (!myShopId.isEmpty()){
            firestore.collection("Seller").get().addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        val sellerId = doc.data.getValue("sellerKey").toString()
                        val verified = doc.data.getValue("verified").toString()
                        if (sellerId.equals(myShopId) || verified.equals("true")){
                            mySellerDocId = doc.id
                            Toast.makeText(view.context, "Shop found", Toast.LENGTH_LONG).show()
                            break
                        }
                    }
                    addShop()
                }
            }
        }
    }

    private fun addShop() {
        val shopIdModel = ShopsDocIdModel(mySellerDocId)
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!)
            .collection("ShopsDocId").add(shopIdModel).addOnCompleteListener {

            }
    }

    fun getAllShops(view: View) {
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!)
            .collection("ShopsDocId").get().addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        val shopDocItem = doc.data.getValue("name").toString()
                        shopDocList.add(shopDocItem)
                    }
                    fetchShops(view)
                }
            }
    }

    private fun fetchShops(view: View) {
        for (i in 0..shopDocList.size-1){
            val shopDocItem = shopDocList[i]
            firestore.collection("Seller").get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                for (myDoc in it.result!!) {
                                    if (myDoc.id.equals(shopDocItem)) {
                                        val companyName = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("companyName").toString())
                                        val companyDescription = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("companyDescription").toString())
                                        val email = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("email").toString())
                                        val fullName = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("fullName").toString())
                                        val phoneNo = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("fullName").toString())
                                        val sellerKey = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("fullName").toString())

                                        firestore.collection("Seller").document(email).collection("Categories").get()
                                            .addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    for (docu in it.result!!){
                                                        val catItem = CategoryModel(docu.data.getValue("name").toString())
                                                        catList.add(catItem)
                                                    }
                                                    getShopProducts(view,companyName,companyDescription,email,fullName,phoneNo,sellerKey)
                                                }
                                            }
                                    }
                                }
                            }
            }
        }
    }

    private fun getShopProducts(view: View, companyName: String?, companyDescription: String?, email: String?, fullName: String?, phoneNo: String?, sellerKey: String?) {
        firestore.collection("Seller").document(email!!).collection("Products").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for (docu in it.result!!){
                        val prodItem = ProductModel(docu.data.getValue("name").toString(), docu.data.getValue("description").toString(),
                            docu.data.getValue("category").toString(), docu.data.getValue("price").toString())
                        prodList.add(prodItem)
                    }
                    finaliseFetch(view, companyName,companyDescription,email,fullName,phoneNo,sellerKey)
                }
            }
    }

    private fun finaliseFetch(view:View , companyName: String?, companyDescription: String?, email: String, fullName: String?, phoneNo: String?, sellerKey: String?) {
        val shopItem = ShopModel(companyName,companyDescription,email,fullName,phoneNo,sellerKey,catList,prodList)
        shopsList.add(shopItem)
        setAdapter(view)
    }


    private fun setAdapter(view: View) {
        if (shopsList.isEmpty()){
            view.findViewById<LinearLayout>(R.id.noShopsLay).visibility = View.VISIBLE
        }else{
            view.findViewById<LinearLayout>(R.id.noShopsLay).visibility = View.INVISIBLE
        }

        val adapter = ShopAdapter(view.context, shopsList)
        val rv = view.findViewById<RecyclerView>(R.id.rvShops)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(view.context)

    }
}