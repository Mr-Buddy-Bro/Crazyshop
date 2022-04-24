package com.tech2develop.crazyshop.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.util.ArrayList

class ProductAdapter(context: Context, list: ArrayList<ProductModel>, categories: ArrayList<String>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    val context = context
    val list = list
    var categories = categories

    companion object{
        lateinit var dialog: Dialog
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.seller_product_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = list[position]

        holder.itemName.text = product.name
        holder.itemCat.text = product.description
        holder.itemPrice.text = "Rs."+product.price
        Picasso.get().load(product.imageUrl).into(holder.itemImage)

        holder.editIcon.contentDescription="edit ${product.name}"
        
        holder.editIcon.setOnClickListener {
            dialog = Dialog(context)
            dialog.setContentView(R.layout.edit_product_dialog_layout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            dialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)

            val diImage = dialog.findViewById<ImageView>(R.id.ivPrAdd)
            dialog.findViewById<EditText>(R.id.etPrName).setText(product.name)
            dialog.findViewById<EditText>(R.id.etPrDesc).setText(product.description)
            dialog.findViewById<EditText>(R.id.etPrPrice).setText(product.price)
            val spCat = dialog.findViewById<Spinner>(R.id.prSpinner)

            categories[0] = product.category.toString()

            val adapter =
                ArrayAdapter(context, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCat.adapter = adapter

            Picasso.get().load(product.imageUrl).into(diImage)

            val switchInStock = dialog.findViewById<Switch>(R.id.switchInStock)

            switchInStock.isChecked = product.inStock

            dialog.show()

            dialog.findViewById<MaterialButton>(R.id.btnChoosePrImage).setOnClickListener {
                val i = Intent(Intent.ACTION_PICK)
                i.type = "image/*"
                SellerHome.myContext.startActivityForResult(i, 201)
            }
            dialog.findViewById<MaterialButton>(R.id.btnSubmitPr).setOnClickListener {
                updateProduct(product, position, diImage, spCat, switchInStock)
            }

            dialog.findViewById<TextView>(R.id.btnDeletePrd).setOnClickListener {
                dialog.dismiss()
                val firestore = FirebaseFirestore.getInstance()
                val storage = FirebaseStorage.getInstance()

                val deleteDialog = Dialog(context)
                deleteDialog.setTitle("Delete product")
                deleteDialog.setContentView(R.layout.delete_prd_dialog)

                deleteDialog.show()
                val loadingDialog = Dialog(context)
                loadingDialog.setContentView(R.layout.loading_layout)
                loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

                deleteDialog.findViewById<GifImageView>(R.id.btnTvDelPr).setOnClickListener {
                    Log.d("newCat", "onBindViewHolder: Clicked to delete")
                    loadingDialog.show()

                    deleteDialog.dismiss()
                    firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
                        .collection("Products").document(list[position].id!!).delete().addOnCompleteListener {
                                storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg").delete().addOnCompleteListener {
                                    loadingDialog.dismiss()
                                }
                        }
                }
            }
        }
    }

    private fun updateProduct(
        product: ProductModel,
        position: Int,
        diImage: ImageView,
        spCat: Spinner,
        switchInStock: Switch
    ) {

        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val name = dialog.findViewById<EditText>(R.id.etPrName).text.toString()
        val desc = dialog.findViewById<EditText>(R.id.etPrDesc).text.toString()
        val price = dialog.findViewById<EditText>(R.id.etPrPrice).text.toString()

        val loadingDialog = Dialog(context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.setCancelable(false)
            loadingDialog.show()

            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
                .collection("Products").document(list[position].id!!).update( "name",name )
                .addOnCompleteListener {
                    if (!it.isSuccessful){
                        Toast.makeText(
                            context,
                            "Something went wrong! please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    dialog.dismiss()
                }
        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
            .collection("Products").document(list[position].id!!).update( "description",desc )
            .addOnCompleteListener {
                if (it.isSuccessful){
                    dialog.dismiss()
                }else {
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        "Something went wrong! please try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
            .collection("Products").document(list[position].id!!).update( "price",price )
            .addOnCompleteListener {
                if (it.isSuccessful){
                    dialog.dismiss()
                }else {
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        "Something went wrong! please try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        if (switchInStock.isChecked != product.inStock){
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
                .collection("Products").document(list[position].id!!).update( "inStock", switchInStock.isChecked)
                .addOnCompleteListener {
                    dialog.dismiss()
                }
        }

    if (spCat.selectedItem.toString() != product.category){
        Log.d("newCat", "updateProduct: Category changed")

        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
            .collection("Products").document(list[position].id!!).update( "category", spCat.selectedItem.toString())
            .addOnCompleteListener {
                dialog.dismiss()
            }
    }

    if (SellerHome.prImageUri != null) {

        val storageRef =
            storage.getReference().child("${SellerHome.shopId}/product images/${name}.jpg")
        storageRef.putFile(SellerHome.prImageUri!!).addOnCompleteListener{

            storageRef.downloadUrl.addOnSuccessListener {
                val imageUrl = it.toString()
                firestore.collection("Seller").document(SellerHome.auth.currentUser?.email.toString())
                    .collection("Products").document(list[position].id!!).update( "imageUrl",imageUrl)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            dialog.dismiss()
                        }else {
                            dialog.dismiss()
                            Toast.makeText(
                                context,
                                "Something went wrong! please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
            Toast.makeText(context,"product updated",Toast.LENGTH_LONG).show()
            loadingDialog.dismiss()
        }

    }
        if (!name.equals(product.name)){

        val storageRef =
            storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name!!, "jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            Log.d("prUpdate", "new:  "+localImage.toUri())
            storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg").delete()

            val newRef = storage.getReference().child("${SellerHome.shopId}/product images/${name}.jpg")
            newRef.putFile(localImage.toUri()).addOnCompleteListener{

                newRef.downloadUrl.addOnSuccessListener {
                    val imageUrl = it.toString()
                    firestore.collection("Seller")
                        .document(SellerHome.auth.currentUser?.email.toString())
                        .collection("Products").document(list[position].id!!)
                        .update("imageUrl", imageUrl)
                        .addOnCompleteListener {task->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                            } else {
                                dialog.dismiss()
                                Toast.makeText(
                                    context,
                                    "Something went wrong! please try again later.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
                Toast.makeText(context,"product updated",Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            }
        }

    }else{
        loadingDialog.dismiss()
    }

    }
    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val itemImage = itemView.findViewById<ImageView>(R.id.imageView2)
        val itemName = itemView.findViewById<TextView>(R.id.textView30)
        val itemCat = itemView.findViewById<TextView>(R.id.textView31)
        val itemPrice = itemView.findViewById<TextView>(R.id.textView33)
        val editIcon = itemView.findViewById<ImageButton>(R.id.btnEditProduct)

    }

}