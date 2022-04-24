package com.tech2develop.crazyshop.ui.buyerFragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.util.jar.Manifest

class BuyerHomeFragment : Fragment(R.layout.fragment_buyer_home) {

    lateinit var firestore : FirebaseFirestore
    lateinit var dialog: Dialog
    var mySellerDocId : String? = null
    lateinit var refreshLay : SwipeRefreshLayout
    companion object {
        lateinit var shopsList: ArrayList<ShopModel>
    }
    lateinit var catList : ArrayList<CategoryModel>
    lateinit var prodList : ArrayList<ProductModel>
    lateinit var sellerDoc : String
    lateinit var shopDocList : ArrayList<String>
    lateinit var loadingDialog  :Dialog
    lateinit var currentView : View
    lateinit var adapter : ShopAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = true

        currentView = view

        val scanQrCode = registerForActivityResult(ScanQRCode(), ::handleResult)

        firestore = FirebaseFirestore.getInstance()
        catList = ArrayList()
        prodList = ArrayList()
        shopDocList = ArrayList()
        shopsList = ArrayList()

        refreshLay = view.findViewById<SwipeRefreshLayout>(R.id.refreshHome)
        refreshLay.setOnRefreshListener {
            loadBanner()
            getAllShops(view)
        }

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        loadingDialog.setCancelable(false)

        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.add_shop_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.setWindowAnimations(R.style.AnimationWindowForAddShopDialog)

        getAllShops(view)

        view.findViewById<MaterialButton>(R.id.btnAddShop).setOnClickListener {
            dialog.show()
        }
        dialog.findViewById<MaterialButton>(R.id.btnAddShopFirm).setOnClickListener {
            val myShopId = dialog.findViewById<EditText>(R.id.editText2).text.toString()
            getShop(view, myShopId)
        }
        dialog.findViewById<MaterialButton>(R.id.materialButton7).setOnClickListener {

            scanQrCode.launch(null)
        }
        loadBanner()

    }

    fun loadBanner(){

        val bannerImageUrlList = ArrayList<String>()
        var bannerUrl=""
        firestore.collection("Banner Urls").get().addOnCompleteListener {
            if ( it.isSuccessful) {
                for (document in it.result!!) {
                    val url = document.data.getValue("bannerUrl")
                    bannerImageUrlList.add(url.toString())
                }
                setBanner(bannerImageUrlList)
            }
        }
    }

    private fun setBanner(bannerImageUrlList: ArrayList<String>) {
        val ivBanner = currentView.findViewById<ImageCarousel>(R.id.bannerImg)
        ivBanner.registerLifecycle(lifecycle)

        val list = mutableListOf<CarouselItem>()

        for (position in 0..bannerImageUrlList.size-1){
            list.add(
                CarouselItem(
                    imageUrl = bannerImageUrlList[position]

                )
            )
        }
        ivBanner.setData(list)

    }

    private fun getShop(view: View, myShopId: String) {
        dialog.dismiss()
        loadingDialog.show()
        if (!myShopId.isEmpty()){
            firestore.collection("Seller").get().addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        val sellerId = doc.data.getValue("sellerKey").toString()
                        val verified = doc.data.getValue("verified").toString()
                        if (sellerId.equals(myShopId) && verified.equals("true")){
                            mySellerDocId = doc.id
                            Toast.makeText(view.context, "Shop found", Toast.LENGTH_LONG).show()
                            addShop()
                            break
                        }
                    }
                    if (mySellerDocId == null){
                        Toast.makeText(view.context, "The shop is not Verified yet", Toast.LENGTH_LONG).show()
                        loadingDialog.dismiss()
                    }

                }
            }
        }else{
            Toast.makeText(view.context, "Invalid QR", Toast.LENGTH_LONG).show()
            loadingDialog.dismiss()
        }
    }

    private fun addShop() {
        val shopIdModel = ShopsDocIdModel(mySellerDocId!!)
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!)
            .collection("ShopsDocId").add(shopIdModel).addOnCompleteListener {
                loadingDialog.dismiss()
                getAllShops(currentView)
            }
    }

    fun getAllShops(view: View) {
        loadingDialog.show()
        shopsList.clear()
        shopDocList.clear()
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
        if (shopDocList.isEmpty()) {
            loadingDialog.dismiss()
            if (shopsList.isEmpty()) {
                view.findViewById<LinearLayout>(R.id.noShopsLay).visibility = View.VISIBLE
            } else {
                view.findViewById<LinearLayout>(R.id.noShopsLay).visibility = View.INVISIBLE
            }
        }

        for (i in 0..shopDocList.size-1){
            val shopDocItem = shopDocList[i]
            firestore.collection("Seller").get().addOnCompleteListener {
                refreshLay.isRefreshing = false
                            if (it.isSuccessful) {
                                for (myDoc in it.result!!) {
                                    if (myDoc.id.equals(shopDocItem)) {
                                        val companyName = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("companyName").toString())
                                        val companyDescription = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("companyDescription").toString())
                                        val email = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("email").toString())
                                        val fullName = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("fullName").toString())
                                        val phoneNo = AESCrypt.decrypt(BuyerHome.eSellerDataKey, myDoc.data.getValue("phoneNo").toString())
                                        val sellerKey = myDoc.data.getValue("sellerKey").toString()
                                        val iconUrl = myDoc.data.getValue("iconUrl").toString()
                                        val bannerUrl = myDoc.data.getValue("bannerUrl").toString()
                                        val shopItem = ShopModel(companyName,companyDescription,email,fullName,phoneNo,sellerKey,null,null, iconUrl, bannerUrl)
                                        shopsList.add(shopItem)
                                    }
                                }
                                setAdapter(view)
                            }
            }
        }
    }

    private fun setAdapter(view: View) {
        if (shopsList.isEmpty()) {
            view.findViewById<LinearLayout>(R.id.noShopsLay).visibility = View.VISIBLE
        } else {
            view.findViewById<LinearLayout>(R.id.noShopsLay).visibility = View.INVISIBLE
        }
        loadingDialog.dismiss()
        Log.d("qrScan", "handleResult: ${shopsList.size}")
        adapter = ShopAdapter(view.context, shopsList)
        val rv = view.findViewById<RecyclerView>(R.id.rvShops)
        rv.isNestedScrollingEnabled = false
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(view.context)
    }

    fun handleResult(qrResult: QRResult?) {

        val result = qrResult.toString()
        val shopKeyPre1 = result.substringAfter("=")
        val shopKeyPre = shopKeyPre1.substringAfter("=")
        val shopKey = shopKeyPre.substringBefore(")")
        Log.d("qrScan", "handleResult: ${shopKey}")
        getShop(currentView, shopKey)

    }
}


