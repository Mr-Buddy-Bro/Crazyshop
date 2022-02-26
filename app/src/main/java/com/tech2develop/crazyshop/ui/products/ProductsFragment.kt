package com.tech2develop.crazyshop.ui.products

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.ui.dashboard.DashboardFragment
import java.util.ArrayList

class ProductsFragment : Fragment(R.layout.fragment_products) {

    lateinit var btnAddProduct: MaterialButton
    lateinit var dialog: Dialog
    val PR_IV_REQ_CODE = 103
    lateinit var categories : ArrayList<String>
    lateinit var adapter : ArrayAdapter<String>
    lateinit var spinner : Spinner

    lateinit var firestore : FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storage : FirebaseStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddProduct = view.findViewById(R.id.materialButton6)

        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.add_product_pop_up)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        spinner = dialog.findViewById<Spinner>(R.id.prSpinner)

        categories = ArrayList()

        btnAddProduct.setOnClickListener {
            dialog.show()
        }

        getCatergories(view)

        dialog.findViewById<MaterialButton>(R.id.btnChoosePrImage).setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i,PR_IV_REQ_CODE)
        }

    }

    private fun getCatergories(view: View) {
        firestore.collection("Seller").document(auth.currentUser?.email.toString())
            .collection("Categories").get().addOnCompleteListener {

                Log.d("TAG", "getCatergories: Cat Completed ${it.result!!.size()}")


                if (it.isSuccessful){
                    for (doc in it.result!!){
                        Log.d("TAG", "getCatergories: Cat success 1")
                        val catName = doc.data.getValue("name").toString()
                        Log.d("TAG", "getCatergories: Cat success ${catName}")
                        categories.add(catName)
                    }
                    adapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, categories)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }else{
                    Log.d("TAG", "getCatergories: Cat failed")
                }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PR_IV_REQ_CODE && resultCode == AppCompatActivity.RESULT_OK){
            val prImageUri = data?.data
            dialog.findViewById<ImageView>(R.id.ivPrAdd).setImageURI(prImageUri)
            dialog.findViewById<ImageView>(R.id.ivPrAdd).visibility = View.VISIBLE
        }
    }

}