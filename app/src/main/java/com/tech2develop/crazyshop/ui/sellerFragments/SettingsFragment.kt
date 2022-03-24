package com.tech2develop.crazyshop.ui.sellerFragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.MainActivity
import com.tech2develop.crazyshop.Models.SettingsModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SettingsFragment : Fragment(R.layout.fragment_settings), PurchasesUpdatedListener {

    lateinit var auth: FirebaseAuth
    val TAG = "TAG"
    lateinit var firestore: FirebaseFirestore
    lateinit var btnActivateOrdering : Switch
    lateinit var tvSetCompanyName : TextView
    lateinit var tvSetDeliveryTime : TextView
    lateinit var tvSetDeliveryCharge : TextView
    lateinit var tvSetOrderingTime : TextView
    lateinit var ivSetIcon : ImageView
    lateinit var ivSetBanner : ImageView
    lateinit var myView : View
    lateinit var loadingDialog : Dialog
    lateinit var cNameChangeDialog: Dialog
    lateinit var dTimeChangeDialog: Dialog
    lateinit var settDocId : String
    lateinit var btnSubscribe : MaterialButton

    lateinit var deliveFromHour : String
    lateinit var deliveFromMin : String
    lateinit var deliveFromNoon : String

    lateinit var deliveToHour : String
    lateinit var deliveToMin : String
    lateinit var deliveToNoon : String

    lateinit var btnChooseFromTime : MaterialButton
    lateinit var btnChooseToTime : MaterialButton
    lateinit var btnSaveDeliveryTime : MaterialButton

    var billingClient: BillingClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myView = view

        auth = FirebaseAuth.getInstance()
        SellerHome.isDashboard = false
        firestore = FirebaseFirestore.getInstance()

        dTimeChangeDialog = Dialog(view.context)
        dTimeChangeDialog.setContentView(R.layout.change_delivery_time_layout)

        btnChooseFromTime = dTimeChangeDialog.findViewById(R.id.materialButton9)
        btnChooseToTime = dTimeChangeDialog.findViewById(R.id.materialButton8)
        btnSaveDeliveryTime = dTimeChangeDialog.findViewById(R.id.btnSaveDeliveryTime)

        btnChooseFromTime.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener{ timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                btnChooseFromTime.text = SimpleDateFormat("HH:mm xm").format(cal.time)
            }
//            DatePickerDialog(view.context, timeSetListener,
//                cal.get(Calendar.YEAR),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)

        cNameChangeDialog = Dialog(view.context)
        cNameChangeDialog.setContentView(R.layout.et_company_name_layout)

        billingClient = SellerHome.billingClient

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

        tvSetCompanyName.setOnClickListener {
            cNameChangeDialog.show()
        }

        cNameChangeDialog.findViewById<MaterialButton>(R.id.btnChangeCName).setOnClickListener {
            val etNewName = cNameChangeDialog.findViewById<EditText>(R.id.etChangeCName)
            if (etNewName.text.toString().equals("") || etNewName.text == null){
                Toast.makeText(myView.context, "Please enter a new Company name", Toast.LENGTH_LONG).show()
            }else{
                loadingDialog.show()
                val newName = AESCrypt.encrypt(SellerHome.eSellerDataKey,etNewName.text.toString())
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).update("companyName", newName).addOnCompleteListener {
                    loadingDialog.dismiss()
                    if (it.isSuccessful){
                        Toast.makeText(myView.context, "Company name changed to $newName", Toast.LENGTH_LONG).show()
                        cNameChangeDialog.dismiss()
                        val i = Intent(myView.context, SellerHome::class.java)
                        myView.context.startActivity(i)
                    }
                }
            }
        }

        tvSetDeliveryTime.setOnClickListener {
            dTimeChangeDialog.show()
        }

        fetchSettingsData()
        if (SellerHome.subscribed){
//            myView.findViewById<MaterialButton>(R.id.btnSubscribe).visibility = View.INVISIBLE
            myView.findViewById<TextView>(R.id.subscriptionStatus).text = "Subscribed"
        }else{
//            myView.findViewById<MaterialButton>(R.id.btnSubscribe).visibility = View.VISIBLE
            myView.findViewById<TextView>(R.id.subscriptionStatus).text = "Expired"
        }
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
        loadingDialog.show()
        var setting : SettingsModel
        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Settings").get()
            .addOnCompleteListener {
                loadingDialog.dismiss()
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

//        val fH = setting.deliveryTime!!.toString().subString(0,2)
//        Log.d("TAG", "displaySetting: $fH")

        when(setting.active){
            true-> btnActivateOrdering.isChecked = true
            false-> btnActivateOrdering.isChecked = false
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handlePurchases(purchases)
            }
            else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
                val alreadyPurchases = queryAlreadyPurchasesResult.purchasesList
                alreadyPurchases?.let { handlePurchases(it) }
            }
            else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(myView.context, "Purchase Canceled", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(myView.context, "Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
            }

    }

    private fun handlePurchases(purchases: List<Purchase>) {

    }
}