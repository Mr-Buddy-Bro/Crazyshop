package com.tech2develop.crazyshop.ui.buyerFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.MainActivity
import com.tech2develop.crazyshop.R

class BuyerSettingsFragment : Fragment(R.layout.fragment_buyer_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = false

        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogOutBuyer)
        btnLogout.setOnClickListener {
            BuyerHome.auth.signOut()
            startActivity(Intent(view.context, MainActivity::class.java))
        }
    }

}