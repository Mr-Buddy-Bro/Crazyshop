package com.tech2develop.crazyshop.ui.sellerFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.MainActivity
import com.tech2develop.crazyshop.Models.SettingsModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment(R.layout.fragment_settings), PurchasesUpdatedListener {

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var btnActivateOrdering : Switch
    lateinit var tvSetCompanyName : TextView
    lateinit var tvSetDeliveryTime : TextView
    lateinit var tvSetDeliveryCharge : TextView
    lateinit var tvSetOrderingTime : TextView
    lateinit var ivSetIcon : ImageView
    lateinit var ivSetBanner : ImageView
    lateinit var myView : View
    lateinit var settDocId : String
    lateinit var btnSubscribe : MaterialButton
    var billingClient: BillingClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        SellerHome.isDashboard = false
        firestore = FirebaseFirestore.getInstance()

        billingClient = SellerHome.billingClient

        myView = view

        btnActivateOrdering = view.findViewById(R.id.btnActivateOrdering)
        tvSetCompanyName = view.findViewById(R.id.tvSetCompanyName)
        tvSetDeliveryTime = view.findViewById(R.id.tvSetDeliveryTime)
        tvSetDeliveryCharge = view.findViewById(R.id.tvSetDeliveryCharge)
        tvSetOrderingTime = view.findViewById(R.id.tvSetOrderingTime)
        ivSetIcon = view.findViewById(R.id.ivSetIcon)
        ivSetBanner = view.findViewById(R.id.ivSetBanner)
        btnSubscribe = view.findViewById(R.id.btnSubscribe)

        btnSubscribe.setOnClickListener {
            onSubscribe()
        }

        btnActivateOrdering.setOnCheckedChangeListener { compoundButton, b ->
            Log.d("TAG", b.toString())
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Settings")
                .document(settDocId).update("active",b)
        }

        view.findViewById<MaterialButton>(R.id.btnLogOut).setOnClickListener {
            auth.signOut()
            startActivity(Intent(view.context, MainActivity::class.java))
        }

        fetchSettingsData()
    }

    private fun onSubscribe() {
        if (billingClient!!.isReady){
            initiatePurchase()
        }else{
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        initiatePurchase()
                    }
                }
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }
    }

    private fun initiatePurchase() {
        Log.d("TAG", "initiatePurchase: 1")
        val skuList = ArrayList<String>()
        skuList.add("seller_subscription_1_month")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        val billingResult = billingClient!!.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            Log.d("TAG", "initiatePurchase: 2")
            billingClient!!.querySkuDetailsAsync(params.build()){
                billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){
                    Log.d("TAG", "initiatePurchase: 3")
                    Log.d("TAG", "initiatePurchase: ${skuDetailsList!!.size}")
                    if (skuDetailsList != null && skuDetailsList.size > 0){
                        Log.d("TAG", "initiatePurchase: 4")
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList[0])
                            .build()
                        billingClient!!.launchBillingFlow(SellerHome.myContext, flowParams)
                    }else{
                        Toast.makeText(myView.context, "Subscription not available", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(myView.context, "Error "+ billingResult.debugMessage, Toast.LENGTH_LONG).show()
                }
            }
        }else{
            Toast.makeText(myView.context, "Sorry, subscription not supported. Please update Play Store", Toast.LENGTH_LONG).show()
        }

        // Process the result.
    }

    private fun fetchSettingsData() {
        var setting : SettingsModel
        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Settings").get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        settDocId = doc.id
                        setting = SettingsModel(doc.data.getValue("active") as Boolean,
                            doc.data.getValue("deliveryTime").toString(), doc.data.getValue("deliveryCharge").toString(),
                            doc.data.getValue("orderingTime").toString())
                        displaySetting(setting)
                    }

                }
            }
    }

    private fun displaySetting(setting: SettingsModel) {
        val companyName = AESCrypt.decrypt(SellerHome.eSellerDataKey, SellerHome.shopName)
        tvSetCompanyName.text = companyName
        tvSetDeliveryTime.text = setting.deliveryTime
        tvSetDeliveryCharge.text = "Rs. "+setting.deliveryCharge
        tvSetOrderingTime.text = setting.orderingTime
        Log.d("TAG", setting.active.toString())
        when(setting.active){
            true-> btnActivateOrdering.isChecked = true
            false-> btnActivateOrdering.isChecked = false
        }
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {

    }

}