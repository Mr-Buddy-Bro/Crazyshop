package com.tech2develop.crazyshop.ui.sellerFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.tech2develop.crazyshop.MainActivity
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        SellerHome.isDashboard = false

        view.findViewById<MaterialButton>(R.id.btnLogOut).setOnClickListener {
            auth.signOut()
            startActivity(Intent(view.context, MainActivity::class.java))
        }
    }

}