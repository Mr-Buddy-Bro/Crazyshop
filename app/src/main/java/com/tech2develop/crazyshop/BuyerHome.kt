package com.tech2develop.crazyshop

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.ui.buyerFragments.*


class BuyerHome : AppCompatActivity() {

    lateinit var firestore : FirebaseFirestore
    lateinit var navView: NavigationView
    val CAM_PERM_REQ_CODE = 1
    lateinit var drawerLayout : DrawerLayout
    lateinit var buyerName : String
    var btnType : String? = null
    lateinit var btnSearchBuyerHome : ImageView
    lateinit var searchDialog : Dialog

    companion object{
        lateinit var storage : FirebaseStorage
        lateinit var auth: FirebaseAuth
        val eSellerDataKey = "sellerDataKey1332"
        val BuyerDataKey = "BuyerData123Key1212"
        var isHome = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_home)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAM_PERM_REQ_CODE)
        }

        searchDialog = Dialog(this)
        searchDialog.setContentView(R.layout.search_dialog_layout)
        searchDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        searchDialog.window!!.setWindowAnimations(R.style.AnimationWindowForSearchDialog)
        btnType = intent.getStringExtra("btnType")

        if (btnType != null){
            if (btnType.equals("myOrders")){
                updateFragment(BuyerMyOrdersFragment())
            }
        }else{
            updateFragment(BuyerHomeFragment())
        }


        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        navView = findViewById(R.id.buyerNavView)
        btnSearchBuyerHome = findViewById(R.id.btnSearchBuyerHome)

        btnSearchBuyerHome.setOnClickListener {
            val window = searchDialog.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.TOP
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = wlp
            searchDialog.show()
        }

        firestore.collection("Buyer").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    if (doc.id == auth.currentUser!!.email) {
                        buyerName = AESCrypt.decrypt(BuyerDataKey,doc.data.getValue("fullName").toString())
                        navView.getHeaderView(0).findViewById<TextView>(R.id.navName).text =
                            buyerName
                        navView.getHeaderView(0).findViewById<TextView>(R.id.textView).text =
                            auth.currentUser!!.email
                        break
                    }
                }
            }
        }


        drawerLayout = findViewById<DrawerLayout>(R.id.buyerDrawerLayout)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home_buyer -> {
                    updateFragment(BuyerHomeFragment())
                }
                R.id.nav_cart_buyer->{
                    updateFragment(BuyerWishListFragment())
                }
                R.id.nav_my_orders_buyer->{
                    updateFragment(BuyerMyOrdersFragment())
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

    fun updateFragment(fragment: Fragment) {
        val fragManager = supportFragmentManager.beginTransaction()
        fragManager.replace(R.id.buyerFragmentContainer, fragment).commit()
    }

    override fun onBackPressed() {
        if(!isHome){
            updateFragment(BuyerHomeFragment())
        }else{
            finishAffinity()
        }
    }

    fun btnNavigationIcon(view: View) {
        if(drawerLayout.isOpen){
            drawerLayout.closeDrawer(navView)
        }else{
            drawerLayout.openDrawer(navView)
        }
    }
}