package com.tech2develop.crazyshop.ui.sellerFragments

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.tech2develop.crazyshop.Adapters.ProductAdapter
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
import java.util.*

class ProductsFragment : Fragment(R.layout.fragment_products) {

    lateinit var btnAddProduct: MaterialButton
    lateinit var dialog: Dialog
    val PR_IV_REQ_CODE = 103
    lateinit var categories: ArrayList<String>
    lateinit var adapter: ArrayAdapter<String>
    lateinit var spinner: Spinner
    lateinit var btnSubmitPr: MaterialButton
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    var prImageUri: Uri? = null
    lateinit var progress: ProgressDialog
    lateinit var productList : ArrayList<ProductModel>
    lateinit var loadingDialog : Dialog
    lateinit var myView : View
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myView = view

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        swipeRefreshLayout = view.findViewById(R.id.pr_swipe_refresh)

        swipeRefreshLayout.setOnRefreshListener {
            getCatergories(view)
            getProducts()
        }

        btnAddProduct = view.findViewById(R.id.materialButton6)
        progress = ProgressDialog(view.context)
        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.add_product_pop_up)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.setWindowAnimations(R.style.AnimationWindowForAddShopDialog)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        SellerHome.isDashboard = false
        spinner = dialog.findViewById<Spinner>(R.id.prSpinner)
        btnSubmitPr = dialog.findViewById(R.id.btnSubmitPr)

        btnSubmitPr.setOnClickListener {
            if(prImageUri != null){
                uploadProduct(view)
            }else{
                Toast.makeText(view.context,"Please choose an image",Toast.LENGTH_LONG).show()
            }

        }
        categories = ArrayList()
        btnAddProduct.setOnClickListener {
            dialog.show()
        }
        getCatergories(view)
        getProducts()
        dialog.findViewById<MaterialButton>(R.id.btnChoosePrImage).setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, PR_IV_REQ_CODE)
        }

    }

    fun getProducts() {
        loadingDialog.show()
        productList = ArrayList()
        productList.clear()
        firestore.collection("Seller").document(auth.currentUser?.email.toString()).collection("Products").get().addOnCompleteListener {
            loadingDialog.dismiss()
            swipeRefreshLayout.isRefreshing = false
            if (it.isSuccessful){
                for (doc in it.result!!){
                    val product = ProductModel(doc.data.getValue("name").toString(),doc.data.getValue("description").toString(),
                        doc.data.getValue("category").toString(),doc.data.getValue("price").toString(), doc.id, doc.data.getValue("imageUrl").toString())
                    productList.add(product)
                }
              setAdapter(myView)
            }else{
                myView.findViewById<TextView>(R.id.tvNoProducts).visibility = View.VISIBLE
            }
        }
    }

    private fun setAdapter(view: View) {
        val adapter = ProductAdapter(view.context, productList)
        val rv = view.findViewById<RecyclerView>(R.id.rvProducts)
        rv.isNestedScrollingEnabled = false
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(view.context)
        rv.visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.tvNoProducts).visibility = View.INVISIBLE
    }

    private fun uploadProduct(view: View) {

        progress.setTitle("Please wait")
        progress.setMessage("Adding product..")
        val itemName = dialog.findViewById<EditText>(R.id.etPrName).text.toString()
        val itemDesc = dialog.findViewById<EditText>(R.id.etPrDesc).text.toString()
        val itemPrice = dialog.findViewById<EditText>(R.id.etPrPrice).text.toString()
        val cat = dialog.findViewById<Spinner>(R.id.prSpinner).selectedItem.toString()
        Log.d("TAG", "uploadProduct: ${cat}")

        if (prImageUri == null){
            Toast.makeText(view.context,"Please choose an image",Toast.LENGTH_LONG).show()
        }else if (cat == " -- Select category -- ") {
            Toast.makeText(view.context,"Please select a category",Toast.LENGTH_LONG).show()
        }else if(itemName.isEmpty() || itemDesc.isEmpty() || itemPrice.isEmpty()){
                Toast.makeText(view.context,"Please fill all the details",Toast.LENGTH_LONG).show()
        }else{
            progress.show()
            val storageRef = storage.getReference().child("${SellerHome.shopId}/product images/${itemName}.jpg")
           storageRef.putFile(prImageUri!!).addOnSuccessListener{
                storageRef.downloadUrl.addOnSuccessListener {

                    val downloadUri : Uri = it
                    val imageUrl = downloadUri.toString()
                    Log.d("imageUrl", "uploadProduct: ${imageUrl}")

                        val product = ProductModel(itemName,itemDesc,cat,itemPrice, null, imageUrl)
                        firestore.collection("Seller").document(auth.currentUser?.email.toString()).collection("Products").add(product).addOnSuccessListener {
                            progress.dismiss()
                            Toast.makeText(view.context,"New product added",Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                            getProducts()
                        }.addOnFailureListener {
                            progress.dismiss()
                            dialog.dismiss()
                            Toast.makeText(view.context,"Something went wrong! please try again later.",Toast.LENGTH_LONG).show()
                        }
                }

            }

        }




    }

    private fun getCatergories(view: View) {
        categories.clear()
        categories.add(" -- Select category -- ")
        firestore.collection("Seller").document(auth.currentUser?.email.toString())
            .collection("Categories").get().addOnCompleteListener {
                Log.d("TAG", "getCatergories: Cat Completed ${it.result!!.size()}")
                if (it.isSuccessful) {
                    for (doc in it.result!!) {
                        Log.d("TAG", "getCatergories: Cat success 1")
                        val catName = doc.data.getValue("name").toString()
                        Log.d("TAG", "getCatergories: Cat success ${catName}")
                        categories.add(catName)
                    }
                    adapter =
                        ArrayAdapter(view.context, android.R.layout.simple_spinner_item, categories)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                } else {
                    Log.d("TAG", "getCatergories: Cat failed")
                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PR_IV_REQ_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val imgUri = data?.data!!
            GlobalScope.launch {
                Log.d("TAG", "onActivityResult: Entered")
                val result = Compress.with(myView.context, imgUri)
                    .setQuality(70)
                    .concrete {
                        withMaxWidth(500f)
                        withMaxHeight(500f)
                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                        withIgnoreIfSmaller(true)
                    }
                    .get(Dispatchers.IO)
                withContext(Dispatchers.Main) {
                    Log.d("TAG", "onActivityResult: ${Formatter.formatShortFileSize(myView.context, result.length())}")

                    prImageUri = result.toUri()

                    dialog.findViewById<ImageView>(R.id.ivPrAdd).setImageURI(prImageUri)
                    dialog.findViewById<ImageView>(R.id.ivPrAdd).visibility = View.VISIBLE
                }
            }


        }

    }

}