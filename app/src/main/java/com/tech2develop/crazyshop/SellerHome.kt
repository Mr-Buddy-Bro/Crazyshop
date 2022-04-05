package com.tech2develop.crazyshop

import android.R.attr.bitmap
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.WriterException
import com.scottyab.aescrypt.AESCrypt
import com.tech2develop.crazyshop.Adapters.ProductAdapter
import com.tech2develop.crazyshop.databinding.ActivitySellerHomeBinding
import com.tech2develop.crazyshop.ui.sellerFragments.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
import java.io.File
import java.util.*


class SellerHome : AppCompatActivity(){

    lateinit var binding : ActivitySellerHomeBinding
    lateinit var firestore : FirebaseFirestore
    lateinit var drawerLayout : DrawerLayout

    companion object{
        lateinit var storage : FirebaseStorage
        lateinit var auth: FirebaseAuth
        lateinit var shopId : String
        val eSellerDataKey = "sellerDataKey1332"
        var isDashboard = false
        var subscribed = false
        lateinit var shopName : String
        lateinit var myContext : Activity
        var prImageUri : Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myContext = this

        updateFragment(DashboardFragment())
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

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
                    updateFragment(AllOrdersFragment())
                }
                R.id.nav_settings->{
                    updateFragment(SettingsFragment())
                }
                R.id.nav_help->{
                    updateFragment(Help_and_feedbackFragment())
                }
                R.id.nav_notifies->{
                    updateFragment(NotificationFragment())
                }
                R.id.nav_share->{
                    val shopIdDialog = Dialog(this)
                    shopIdDialog.setContentView(R.layout.shop_id_layout)
                    shopIdDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                    shopIdDialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)
                    shopIdDialog.findViewById<TextView>(R.id.textView60).text = shopId
                    val qrgEncoder =
                        QRGEncoder(shopId, null, QRGContents.Type.TEXT, 600 )
                    qrgEncoder.colorBlack = Color.BLACK
                    qrgEncoder.colorWhite = Color.WHITE
                    try {
                        // Getting QR-Code as Bitmap
                        val bitmap = qrgEncoder.bitmap
                        // Setting Bitmap to ImageView
                        shopIdDialog.findViewById<ImageView>(R.id.imageView5).setImageBitmap(bitmap)
                    } catch (e: WriterException) {
                        Log.v("TAG", e.toString())
                    }
                    shopIdDialog.show()
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
                        shopName = doc.data.getValue("companyName").toString()
                        val email = doc.data.getValue("email").toString()
                        val decShopName = AESCrypt.decrypt(eSellerDataKey, shopName)
                        val decEmail = AESCrypt.decrypt(eSellerDataKey, email)
                        binding.tvToolbarTitle.text = decShopName
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.navName).text = decShopName
                        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView).text = decEmail
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
        }
    }

    fun updateFragment(fragment : Fragment){
        val fragManager = supportFragmentManager.beginTransaction()
        fragManager.replace(R.id.fragmentContainer, fragment).commit()
    }

    override fun onBackPressed() {
        if(!isDashboard){
            updateFragment(DashboardFragment())
        }else{
            finishAffinity()
        }

    }

    fun btnNavigationIcon(view: View) {
        if (!drawerLayout.isOpen){
            drawerLayout.openDrawer(binding.navView)
        }else{
            drawerLayout.closeDrawer(binding.navView)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 201 && resultCode == RESULT_OK){
            val imgUri = data?.data!!
            GlobalScope.launch {
                Log.d("TAG", "onActivityResult: Entered")
                val result = Compress.with(myContext, imgUri)
                    .setQuality(70)
                    .concrete {
                        withMaxWidth(500f)
                        withMaxHeight(500f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)
                withContext(Dispatchers.Main) {
                    Log.d("TAG", "onActivityResult: ${Formatter.formatShortFileSize(myContext, result.length())}")

                    prImageUri = result.toUri()
                    ProductAdapter.dialog.findViewById<ImageView>(R.id.ivPrAdd).setImageURI(prImageUri)
                }
            }
        }
    }
}