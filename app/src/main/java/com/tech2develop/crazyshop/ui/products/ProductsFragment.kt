package com.tech2develop.crazyshop.ui.products

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Adapters.ProductAdapter
import com.tech2develop.crazyshop.GraphicsActivity
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.io.File
import java.util.ArrayList

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddProduct = view.findViewById(R.id.materialButton6)
        progress = ProgressDialog(view.context)
        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.add_product_pop_up)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        spinner = dialog.findViewById<Spinner>(R.id.prSpinner)
        btnSubmitPr = dialog.findViewById(R.id.btnSubmitPr)
        btnSubmitPr.setOnClickListener {
            uploadProduct(view)
        }
        categories = ArrayList()
        btnAddProduct.setOnClickListener {
            dialog.show()
        }
        getCatergories(view)
        getProducts(view)
        dialog.findViewById<MaterialButton>(R.id.btnChoosePrImage).setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, PR_IV_REQ_CODE)
        }

    }

    private fun getProducts(view: View) {
        productList = ArrayList()
        firestore.collection("Seller").document(auth.currentUser?.email.toString()).collection("Products").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    val product = ProductModel(doc.data.getValue("name").toString(),doc.data.getValue("description").toString(),
                        doc.data.getValue("category").toString(),doc.data.getValue("price").toString())
                    productList.add(product)
                }
              setAdapter(view)
            }
        }
    }

    private fun setAdapter(view: View) {
        val adapter = ProductAdapter(view.context, productList)
        val rv = view.findViewById<RecyclerView>(R.id.rvProducts)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(view.context)
        rv.visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.tvNoProducts).visibility = View.INVISIBLE
    }

    private fun uploadProduct(view: View) {

        progress.setTitle("Please wait")
        progress.setMessage("Adding product..")
        progress.show()
        val itemName = dialog.findViewById<EditText>(R.id.etPrName).text.toString()
        val itemDesc = dialog.findViewById<EditText>(R.id.etPrDesc).text.toString()
        val itemPrice = dialog.findViewById<EditText>(R.id.etPrPrice).text.toString()
        val cat = dialog.findViewById<Spinner>(R.id.prSpinner).selectedItem.toString()
        Log.d("TAG", "uploadProduct: ${cat}")
        val product = ProductModel(itemName,itemDesc,cat,itemPrice)
        if (prImageUri == null){
            Toast.makeText(view.context,"Please choose an image",Toast.LENGTH_LONG).show()
        }else if (cat.equals(" -- Select -- ")) {
            Toast.makeText(view.context,"Please select a category",Toast.LENGTH_LONG).show()
        }else if(itemName.isEmpty() || itemDesc.isEmpty() || itemPrice.isEmpty()){
                Toast.makeText(view.context,"Please fill all the details",Toast.LENGTH_LONG).show()
        }else{
            firestore.collection("Seller").document(auth.currentUser?.email.toString()).collection("Products").add(product).addOnSuccessListener {
                progress.dismiss()
                Toast.makeText(view.context,"New product added",Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                progress.dismiss()
                dialog.dismiss()
                Toast.makeText(view.context,"Something went wrong! please try again later.",Toast.LENGTH_LONG).show()
            }
        }
        val storageRef = storage.getReference().child("${SellerHome.shopId}/product images/${itemName}.jpg")
        storageRef.putFile(prImageUri!!)

    }

    private fun getCatergories(view: View) {

        categories.add(" -- Select -- ")
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
                prImageUri = data?.data!!
                dialog.findViewById<ImageView>(R.id.ivPrAdd).setImageURI(prImageUri)
                dialog.findViewById<ImageView>(R.id.ivPrAdd).visibility = View.VISIBLE
        }

    }

}