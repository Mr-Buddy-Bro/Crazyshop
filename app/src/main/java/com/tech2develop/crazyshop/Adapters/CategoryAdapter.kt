package com.tech2develop.crazyshop.Adapters

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.tech2develop.crazyshop.Models.CategoryModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ui.categories.CategoriesFragment
import java.util.ArrayList

class CategoryAdapter(context: Context, list: ArrayList<CategoryModel>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    val context = context
    val list = list
    lateinit var progress  : ProgressDialog
    lateinit var dialog: Dialog
    lateinit var catName: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.category_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val cat = list[position]
        progress = ProgressDialog(context)
        dialog = Dialog(context)
        dialog.setContentView(R.layout.delete_cat_pop_up)

        holder.catName.text = cat.name
        holder.btnDelete.setOnClickListener {
            dialog.show()
            catName = cat.name!!
        }

        dialog.findViewById<MaterialButton>(R.id.btnDeleteCatConfirm).setOnClickListener {
            progress.setMessage("deleting...")

            val pass = dialog.findViewById<EditText>(R.id.etDelPass).text.toString()
            var realPass = ""

            if (pass.isEmpty()){
                Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
            }else{
                progress.show()
                SellerHome.firestore.collection("Seller").get()
                    .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        for (doc in task.result!!){
                            if (doc.data.getValue("email").toString() == SellerHome.auth.currentUser?.email!!){
                                realPass = doc.data.getValue("password").toString()
                                break
                            }
                        }

                        if (pass == realPass){
                            deleteItem(catName)
                        }else{
                            progress.dismiss()
                            Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        progress.dismiss()
                        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }

    private fun deleteItem(cat: String) {
        var docId = ""
        SellerHome.firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Categories")
            .get().addOnCompleteListener {
                progress.dismiss()
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        if (doc.data.getValue("name").toString()==cat){
                            docId = doc.id
                            SellerHome.firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("Categories")
                                .document(docId).delete().addOnCompleteListener {
                                    progress.dismiss()
                                    dialog.dismiss()
                                    Toast.makeText(context,"category deleted",Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                else{
                    progress.dismiss()
                    Toast.makeText(context,"something went wrong",Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val catName = itemView.findViewById<TextView>(R.id.tvCatName)
        val btnDelete = itemView.findViewById<ImageView>(R.id.btnDeleteCat)

    }

}