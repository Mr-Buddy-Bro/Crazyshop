package com.tech2develop.crazyshop

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.ui.buyerFragments.*
import com.tech2develop.crazyshop.ui.sellerFragments.*

class BuyerHome : AppCompatActivity() {

    lateinit var firestore : FirebaseFirestore
    lateinit var navView: NavigationView
    val CAM_PERM_REQ_CODE = 1

    companion object{
        lateinit var storage : FirebaseStorage
        lateinit var auth: FirebaseAuth
        val eSellerDataKey = "sellerDataKey1332"
        var isHome = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_home)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAM_PERM_REQ_CODE)
        }

        supportActionBar?.setTitle(Html.fromHtml("<font color='#000000'>Open shop</font>"))

        updateFragment(BuyerHomeFragment())
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        navView = findViewById(R.id.buyerNavView)

        val drawerLayout = findViewById<DrawerLayout>(R.id.buyerDrawerLayout)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home_buyer -> {
                    updateFragment(BuyerHomeFragment())
                }
                R.id.nav_cart_buyer->{
                    updateFragment(BuyerCartFragment())
                }
                R.id.nav_my_orders_buyer->{
                    updateFragment(BuyerMyOrdersFragment())
                }
                R.id.nav_delivery_address_buyer->{
                    updateFragment(BuyerAddressesFragment())
                }
                R.id.nav_help_buyer->{
                    updateFragment(BuyerHelpFragment())
                }
                R.id.nav_settings_buyer->{
                    updateFragment(BuyerSettingsFragment())
                }
            }
            drawerLayout.closeDrawer(navView)
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAM_PERM_REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFragment(fragment: Fragment) {
        val fragManager = supportFragmentManager.beginTransaction()
        fragManager.replace(R.id.buyerFragmentContainer, fragment).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(!isHome){
            updateFragment(BuyerHomeFragment())
        }else{
            finishAffinity()
        }
    }
}