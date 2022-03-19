package com.tech2develop.crazyshop.ui.sellerFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.MainActivity
import com.tech2develop.crazyshop.Models.SettingsModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome

class SettingsFragment : Fragment(R.layout.fragment_settings) {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        SellerHome.isDashboard = false
        firestore = FirebaseFirestore.getInstance()

        myView = view

        btnActivateOrdering = view.findViewById(R.id.btnActivateOrdering)
        tvSetCompanyName = view.findViewById(R.id.tvSetCompanyName)
        tvSetDeliveryTime = view.findViewById(R.id.tvSetDeliveryTime)
        tvSetDeliveryCharge = view.findViewById(R.id.tvSetDeliveryCharge)
        tvSetOrderingTime = view.findViewById(R.id.tvSetOrderingTime)
        ivSetIcon = view.findViewById(R.id.ivSetIcon)
        ivSetBanner = view.findViewById(R.id.ivSetBanner)

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

}