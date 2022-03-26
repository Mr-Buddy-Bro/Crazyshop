package com.tech2develop.crazyshop.ui.sellerFragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome

class NotificationFragment : Fragment (R.layout.fragment_notification){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SellerHome.isDashboard = false
    }
}