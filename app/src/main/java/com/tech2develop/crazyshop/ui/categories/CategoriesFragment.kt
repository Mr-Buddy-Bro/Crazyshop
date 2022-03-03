package com.tech2develop.crazyshop.ui.categories

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tech2develop.crazyshop.Adapters.CategoryAdapter
import com.tech2develop.crazyshop.Models.CategoryModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome

class CategoriesFragment : Fragment(R.layout.fragment_category) {

    lateinit var dialog : Dialog
    lateinit var progress: ProgressDialog
    lateinit var catList: ArrayList<CategoryModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = Dialog(view.context)
        progress = ProgressDialog(view.context)
        dialog.setContentView(R.layout.add_cat_pop_up)

        view.findViewById<MaterialButton>(R.id.btnAddCat).setOnClickListener {
            dialog.show()
        }
        dialog.findViewById<MaterialButton>(R.id.btnSubmitCat).setOnClickListener {
            addCat(view)
        }

        getCategories(view)

    }

    fun getCategories(view: View) {

        progress.setMessage("please wait..")
        progress.show()

        catList = ArrayList()

        SellerHome.firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
            .collection("Categories").get().addOnCompleteListener {
                progress.dismiss()
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        val cat = CategoryModel(doc.data.getValue("name").toString())
                        catList.add(cat)
                    }
                    setAdapter(view)
                }
            }
    }

    private fun setAdapter(view: View) {
        view.findViewById<TextView>(R.id.tvNoCat).visibility = View.INVISIBLE
        val adapter = CategoryAdapter(view.context, catList)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategories)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(view.context)
    }

    private fun addCat(view: View) {
        progress.setTitle("Adding category")
        progress.setMessage("Please wait...")
        val catName = dialog.findViewById<EditText>(R.id.etCatName).text.toString()
        if(catName.isEmpty()){
            Toast.makeText(view.context,"Please fill category name",Toast.LENGTH_LONG).show()
        }else{
            progress.show()
            val cat = CategoryModel(catName)
            SellerHome.firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("Categories").add(cat).addOnCompleteListener {
                    if (it.isSuccessful){
                        progress.dismiss()
                        dialog.dismiss()
                        getCategories(view)
                        Toast.makeText(view.context,"New category added",Toast.LENGTH_LONG).show()
                    }else{
                        progress.dismiss()
                        Toast.makeText(view.context,"Something went wrong! please try again",Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

}