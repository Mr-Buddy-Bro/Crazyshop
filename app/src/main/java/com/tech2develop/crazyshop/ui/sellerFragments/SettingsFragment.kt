package com.tech2develop.crazyshop.ui.sellerFragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.MainActivity
import com.tech2develop.crazyshop.Models.SettingsModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SettingsFragment : Fragment(R.layout.fragment_settings), PurchasesUpdatedListener {

    lateinit var auth: FirebaseAuth
    val TAG = "TAG"
    lateinit var firestore: FirebaseFirestore

    lateinit var btnActivateOrdering: Switch

    lateinit var tvSetCompanyName: TextView
    lateinit var tvSetDeliveryTime: TextView
    lateinit var tvSetDeliveryCharge: TextView
    lateinit var tvSetOrderingTime: TextView

    lateinit var tvSetCompanyName_edit: ImageView
    lateinit var tvSetDeliveryTime_edit: ImageView
    lateinit var tvSetDeliveryCharge_edit: ImageView
    lateinit var tvSetOrderingTime_edit: ImageView

    lateinit var ivSetIcon: ImageView
    lateinit var ivSetBanner: ImageView
    lateinit var myView: View
    lateinit var loadingDialog: Dialog

    lateinit var cNameChangeDialog: Dialog
    lateinit var dTimeChangeDialog: Dialog
    lateinit var dDelChargeDialog: Dialog

    lateinit var settDocId: String
    lateinit var btnSubscribe: MaterialButton

    lateinit var btnChooseFromTime: MaterialButton
    lateinit var btnChooseToTime: MaterialButton
    lateinit var btnSaveDeliveryTime: MaterialButton

    lateinit var timeFromOrTo : String
    var timeDelOrOrder : String = ""

    var billingClient: BillingClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myView = view

        auth = FirebaseAuth.getInstance()
        SellerHome.isDashboard = false
        firestore = FirebaseFirestore.getInstance()

        dTimeChangeDialog = Dialog(view.context)
        dTimeChangeDialog.setContentView(R.layout.change_delivery_time_layout)
        dTimeChangeDialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)

        dDelChargeDialog = Dialog(view.context)
        dDelChargeDialog.setContentView(R.layout.del_charge_dialog_lay)
        dDelChargeDialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)

        btnChooseFromTime = dTimeChangeDialog.findViewById(R.id.materialButton9)
        btnChooseToTime = dTimeChangeDialog.findViewById(R.id.materialButton8)
        btnSaveDeliveryTime = dTimeChangeDialog.findViewById(R.id.btnSaveDeliveryTime)

        btnActivateOrdering = view.findViewById(R.id.btnActivateOrdering)
        tvSetCompanyName = view.findViewById(R.id.tvSetCompanyName)
        tvSetDeliveryTime = view.findViewById(R.id.tvSetDeliveryTime)
        tvSetDeliveryCharge = view.findViewById(R.id.tvSetDeliveryCharge)
        tvSetOrderingTime = view.findViewById(R.id.tvSetOrderingTime)
        ivSetIcon = view.findViewById(R.id.iv_set_icon)
        ivSetBanner = view.findViewById(R.id.iv_set_banner)
        btnSubscribe = view.findViewById(R.id.btnSubscribe)

        tvSetCompanyName_edit = view.findViewById(R.id.tvSetCompanyName_edit)
        tvSetDeliveryTime_edit = view.findViewById(R.id.tvSetDeliveryTime_edit)
        tvSetDeliveryCharge_edit = view.findViewById(R.id.tvSetDeliveryCharge_edit)
        tvSetOrderingTime_edit = view.findViewById(R.id.tvSetOrderingTime_edit)

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)

        cNameChangeDialog = Dialog(view.context)
        cNameChangeDialog.setContentView(R.layout.et_company_name_layout)
        cNameChangeDialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)

        billingClient = SellerHome.billingClient

        btnSubscribe.setOnClickListener {
            onSubscribe()
        }

        btnActivateOrdering.setOnCheckedChangeListener { compoundButton, b ->
            Log.d("TAG", b.toString())
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("Settings")
                .document(settDocId).update("active", b)
        }

        view.findViewById<MaterialButton>(R.id.btnLogOut).setOnClickListener {
            auth.signOut()
            startActivity(Intent(view.context, MainActivity::class.java))
        }

        tvSetCompanyName_edit.setOnClickListener {
            cNameChangeDialog.show()
        }

        tvSetDeliveryCharge_edit.setOnClickListener {
            timeDelOrOrder = "del"
            dDelChargeDialog.show()
        }

        dDelChargeDialog.findViewById<MaterialButton>(R.id.btnSaveDelCharge).setOnClickListener {
            val newCharge =
                dDelChargeDialog.findViewById<EditText>(R.id.etDelCharge).text.toString()

            if (newCharge.equals("")) {
                Toast.makeText(
                    view.context,
                    "Please enter a new delivery charge",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                dDelChargeDialog.dismiss()
                loadingDialog.show()
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                    .collection("Settings").get()
                    .addOnCompleteListener {
                        var docId: String
                        if (it.isSuccessful) {
                            for (doc in it.result!!) {
                                docId = doc.id
                                firestore.collection("Seller")
                                    .document(SellerHome.auth.currentUser?.email!!)
                                    .collection("Settings").document(docId)
                                    .update("deliveryCharge", newCharge).addOnCompleteListener {
                                        loadingDialog.dismiss()
                                        fetchSettingsData()
                                    }
                            }

                        }
                    }
            }
        }

        ivSetIcon.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, 202)
        }

        ivSetBanner.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, 203)
        }

        tvSetOrderingTime_edit.setOnClickListener {
            timeDelOrOrder = "order"
            dTimeChangeDialog.findViewById<TextView>(R.id.textView73).text =
                "Set time for customers to order from your shop"
            dTimeChangeDialog.show()
        }

/////////////////////////////////////////////////////////////////////////////
        val timePickerDialog = Dialog(view.context)
        timePickerDialog.setContentView(R.layout.time_picker_dialog_layout)
        timePickerDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        timePickerDialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)
        timePickerDialog.window!!.setGravity(Gravity.BOTTOM)
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)

        btnChooseFromTime.setOnClickListener {
            timePickerDialog.show()
            timeFromOrTo = "from"
        }

        btnChooseToTime.setOnClickListener {
            timePickerDialog.show()
            timeFromOrTo = "to"
        }

        timePickerDialog.findViewById<MaterialButton>(R.id.btnPickTime).setOnClickListener {
            val timePicker = timePickerDialog.findViewById<TimePicker>(R.id.timePicker)
            var mHour : Int = 0
            val xm : String
            if (timePicker.currentHour > 12){
                mHour = timePicker.currentHour - 12
                xm = "pm"
            }else{
                mHour = timePicker.currentHour
                xm = "am"
            }

            val mMin = timePicker.currentMinute
            Log.d("time", "onViewCreated: $mHour : $mMin $xm" )

            val time : String

            if(mMin.toString().length < 2){
                time = "$mHour:0$mMin $xm"
            }else{
                time = "$mHour:$mMin $xm"
            }

            if (timeFromOrTo.equals("from")) {
                btnChooseFromTime.text = time
            }else{
                btnChooseToTime.text = time
            }
            timePickerDialog.dismiss()
        }

        btnSaveDeliveryTime.setOnClickListener {
            loadingDialog.show()
            val newTime = "${btnChooseFromTime.text} - ${btnChooseToTime.text}"
            if (timeDelOrOrder == "order") {
                Log.d("newTime", "new Ordering time: $newTime")
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Settings").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            var docId: String
                                for (doc in it.result!!) {
                                    docId = doc.id
                                    firestore.collection("Seller")
                                        .document(SellerHome.auth.currentUser?.email!!)
                                        .collection("Settings").document(docId)
                                        .update("orderingTime", newTime).addOnCompleteListener {
                                            loadingDialog.dismiss()
                                            dDelChargeDialog.dismiss()
                                            fetchSettingsData()
                                        }
                                }

                        }
                    }
            }else{
                Log.d("newTime", "new Delivery time: $newTime")
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Settings").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            var docId: String
                            for (doc in it.result!!) {
                                docId = doc.id
                                firestore.collection("Seller")
                                    .document(SellerHome.auth.currentUser?.email!!)
                                    .collection("Settings").document(docId)
                                    .update("deliveryTime", newTime).addOnCompleteListener {
                                        loadingDialog.dismiss()
                                        dDelChargeDialog.dismiss()
                                        fetchSettingsData()
                                    }
                            }

                        }
                    }
            }
        }

//////////////////////////////////////////////////////////////////////////

        cNameChangeDialog.findViewById<MaterialButton>(R.id.btnChangeCName).setOnClickListener {
            val etNewName = cNameChangeDialog.findViewById<EditText>(R.id.etChangeCName)
            if (etNewName.text.toString().equals("") || etNewName.text == null) {
                Toast.makeText(myView.context, "Please enter a new Company name", Toast.LENGTH_LONG)
                    .show()
            } else {
                loadingDialog.show()
                val newName = AESCrypt.encrypt(SellerHome.eSellerDataKey, etNewName.text.toString())
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                    .update("companyName", newName).addOnCompleteListener {
                    loadingDialog.dismiss()
                    if (it.isSuccessful) {
                        Toast.makeText(
                            myView.context,
                            "Company name changed to ${etNewName.text}",
                            Toast.LENGTH_LONG
                        ).show()
                        cNameChangeDialog.dismiss()
                        val i = Intent(myView.context, SellerHome::class.java)
                        myView.context.startActivity(i)
                    }
                }
            }
        }

        tvSetDeliveryTime_edit.setOnClickListener {
            dTimeChangeDialog.show()
        }

        fetchSettingsData()
        if (SellerHome.subscribed) {
            myView.findViewById<MaterialButton>(R.id.btnSubscribe).visibility = View.INVISIBLE
            myView.findViewById<TextView>(R.id.subscriptionStatus).text = "Subscribed"
        } else {
            myView.findViewById<MaterialButton>(R.id.btnSubscribe).visibility = View.VISIBLE
            myView.findViewById<TextView>(R.id.subscriptionStatus).text = "Expired"
        }


    }

    private fun onSubscribe() {
        if (billingClient!!.isReady) {
            initiatePurchase()
        } else {
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
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
        val billingResult =
            billingClient!!.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d("TAG", "initiatePurchase: 2")
            billingClient!!.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("TAG", "initiatePurchase: 3")
                    Log.d("TAG", "initiatePurchase: ${skuDetailsList!!.size}")
                    if (skuDetailsList != null && skuDetailsList.size > 0) {
                        Log.d("TAG", "initiatePurchase: 4")
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList[0])
                            .build()
                        billingClient!!.launchBillingFlow(SellerHome.myContext, flowParams)
                    } else {
                        Toast.makeText(
                            myView.context,
                            "Subscription not available",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        myView.context,
                        "Error " + billingResult.debugMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                myView.context,
                "Sorry, subscription not supported. Please update Play Store",
                Toast.LENGTH_LONG
            ).show()
        }

        // Process the result.
    }

    private fun fetchSettingsData() {
        loadingDialog.show()
        var setting: SettingsModel
        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
            .collection("Settings").get()
            .addOnCompleteListener {
                loadingDialog.dismiss()
                if (it.isSuccessful) {
                    for (doc in it.result!!) {
                        settDocId = doc.id
                        setting = SettingsModel(
                            doc.data.getValue("active") as Boolean,
                            doc.data.getValue("deliveryTime").toString(),
                            doc.data.getValue("deliveryCharge").toString(),
                            doc.data.getValue("orderingTime").toString()
                        )
                        displaySetting(setting)
                    }

                }
            }

        val storage: FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef = storage.getReference().child("${SellerHome.shopId}/shop graphics/icon.jpg")
        val storageRef1 =
            storage.getReference().child("${SellerHome.shopId}/shop graphics/banner.jpg")
        val localImage = File.createTempFile("icon", "jpg")
        val localImage1 = File.createTempFile("icon", "jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage.absolutePath)

            ivSetIcon.setImageBitmap(bitmapImage)
        }
        storageRef1.getFile(localImage1).addOnSuccessListener {
            val bitmapImage = BitmapFactory.decodeFile(localImage1.absolutePath)

            ivSetBanner.setImageBitmap(bitmapImage)
        }
    }

    private fun displaySetting(setting: SettingsModel) {
        val companyName = AESCrypt.decrypt(SellerHome.eSellerDataKey, SellerHome.shopName)
        tvSetCompanyName.text = companyName
        tvSetDeliveryTime.text = setting.deliveryTime
        tvSetDeliveryCharge.text = "Rs. " + setting.deliveryCharge
        tvSetOrderingTime.text = setting.orderingTime
        Log.d("TAG", setting.active.toString())

        when (setting.active) {
            true -> btnActivateOrdering.isChecked = true
            false -> btnActivateOrdering.isChecked = false
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            val queryAlreadyPurchasesResult =
                billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
            val alreadyPurchases = queryAlreadyPurchasesResult.purchasesList
            alreadyPurchases?.let { handlePurchases(it) }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(myView.context, "Purchase Canceled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                myView.context,
                "Error " + billingResult.debugMessage,
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun handlePurchases(purchases: List<Purchase>) {
        fetchSettingsData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 202 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            ivSetIcon.setImageURI(imageUri)

            GlobalScope.launch {
                Log.d("TAG", "onActivityResult: Entered")
                val result = Compress.with(myView.context, imageUri!!)
                    .setQuality(70)
                    .concrete {
                        withMaxWidth(500f)
                        withMaxHeight(500f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)
                withContext(Dispatchers.Main) {
                    Log.d(
                        "TAG",
                        "onActivityResult: ${
                            Formatter.formatShortFileSize(
                                myView.context,
                                result.length()
                            )
                        }"
                    )

                    val prImageUri = result.toUri()

                    updateGraphics("icon", prImageUri)
                }

            }
        }else if (requestCode == 203 && resultCode == RESULT_OK){

            val imageUri = data?.data
            ivSetBanner.setImageURI(imageUri)

            GlobalScope.launch {
                Log.d("TAG", "onActivityResult: Entered")
                val result = Compress.with(myView.context, imageUri!!)
                    .setQuality(70)
                    .concrete {
                        withMaxWidth(500f)
                        withMaxHeight(500f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)
                withContext(Dispatchers.Main) {
                    Log.d(
                        "TAG",
                        "onActivityResult: ${
                            Formatter.formatShortFileSize(
                                myView.context,
                                result.length()
                            )
                        }"
                    )

                    val prImageUri = result.toUri()

                    updateGraphics("banner", prImageUri)
                }
            }
        }
    }

    private fun updateGraphics(s: String, prImageUri: Uri) {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.getReference().child("${SellerHome.shopId}/shop graphics/${s}.jpg")
        ref.putFile(prImageUri)
    }
}


