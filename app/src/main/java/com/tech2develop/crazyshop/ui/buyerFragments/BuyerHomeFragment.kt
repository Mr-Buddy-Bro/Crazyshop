package com.tech2develop.crazyshop.ui.buyerFragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.R

class BuyerHomeFragment : Fragment(R.layout.fragment_buyer_home) {

    lateinit var firestore : FirebaseFirestore
    lateinit var dialog: Dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = true

        firestore = FirebaseFirestore.getInstance()

        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.add_shop_dialog)

        view.findViewById<MaterialButton>(R.id.btnAddShop).setOnClickListener {
            dialog.show()
        }
        dialog.findViewById<MaterialButton>(R.id.btnAddShopFirm).setOnClickListener {
            addShop()
        }

    }

    private fun addShop() {

    }

}