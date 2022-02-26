package com.tech2develop.crazyshop

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.databinding.ActivitySellerHomeBinding
import com.tech2develop.crazyshop.ui.categories.CategoriesFragment
import com.tech2develop.crazyshop.ui.dashboard.DashboardFragment
import com.tech2develop.crazyshop.ui.products.ProductsFragment
import com.tech2develop.crazyshop.ui.settings.SettingsFragment
import org.w3c.dom.Text
import java.io.File

class SellerHome : AppCompatActivity() {

    lateinit var binding : ActivitySellerHomeBinding
    lateinit var firestore : FirebaseFirestore
    lateinit var storage : FirebaseStorage
    lateinit var auth: FirebaseAuth
    lateinit var shopId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle(Html.fromHtml("<font color='#000000'>Loading..</font>"))

        updateFragment(DashboardFragment())
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_dashboard -> {
                    updateFragment(DashboardFragment())
                }
                R.id.nav_products->{
                    updateFragment(ProductsFragment())
                }
                R.id.nav_cat->{
                    updateFragment(CategoriesFragment())
                }
                R.id.nav_all_orders->{
                    updateFragment(ProductsFragment())
                }
                R.id.nav_settings->{
                    updateFragment(SettingsFragment())
                }

            }
            drawerLayout.closeDrawer(binding.navView)
            true

        }


        firestore.collection("Seller").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    if (doc.id == auth.currentUser?.email!!){
                        shopId = doc.data.getValue("sellerKey").toString()
                        val shopName = doc.data.getValue("companyName").toString()
                        val email = doc.data.getValue("email").toString()
                        supportActionBar?.setTitle(Html.fromHtml("<font color='#000000'>${shopName}</font>"))
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.navName).text = shopName
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView).text = email
                        setHeaderGraphics()
                        break
                    }
                }
            }
        }

    }
    fun setHeaderGraphics(){
        Log.d("TAG", "setHeaderGraphics: ${shopId}")
        val iconStorageRef = storage.getReference().child("${shopId}/shop graphics/icon.jpg")
        val bannerStorageRef = storage.getReference().child("${shopId}/shop graphics/banner.jpg")

        val iconLocalFile = File.createTempFile("icon","jpg")
        val bannerLocalFile = File.createTempFile("banner","jpg")

        iconStorageRef.getFile(iconLocalFile).addOnSuccessListener{
            val bitmap = BitmapFactory.decodeFile(iconLocalFile.absolutePath)
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView).setImageBitmap(bitmap)
        }.addOnFailureListener{
            Log.d("TAG", "setHeaderGraphics: Failed to fetch${it.message}")
        }
        bannerStorageRef.getFile(bannerLocalFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(bannerLocalFile.absolutePath)
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.navBg).setImageBitmap(bitmap)
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.navBg).rotation = -90f
        }
    }

    fun updateFragment(fragment : Fragment){
        val fragManager = supportFragmentManager.beginTransaction()
        val fragmentTransaction = fragManager.replace(R.id.fragmentContainer, fragment).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    fun navClick(view: android.view.View) {
        Log.d("TAG", "onCreate: Clicked")
        view.findViewById<LinearLayout>(R.id.navHeader).setBackgroundResource(R.drawable.logo)
    }
}