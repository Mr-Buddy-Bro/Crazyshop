package com.tech2develop.crazyshop.ui.buyerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.R

class BuyerAddressesFragment : Fragment(R.layout.fragment_buyer_addresses) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = false


    }
}