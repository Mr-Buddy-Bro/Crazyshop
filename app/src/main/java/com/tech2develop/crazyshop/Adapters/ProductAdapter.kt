package com.tech2develop.crazyshop.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.tech2develop.crazyshop.Models.ProductModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import com.tech2develop.crazyshop.ui.sellerFragments.ProductsFragment
import java.io.File
import java.util.ArrayList

class ProductAdapter(context: Context, list: ArrayList<ProductModel>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    val context = context
    val list = list
    lateinit var imagBitmap : Bitmap

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
        getPrImages(null, holder, product)

        holder.editIcon.setOnClickListener {
            dialog = Dialog(context)
            dialog.setContentView(R.layout.edit_product_dialog_layout)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            dialog.window!!.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog)

            val diImage = dialog.findViewById<ImageView>(R.id.ivPrAdd)
            dialog.findViewById<EditText>(R.id.etPrName).setText(product.name)
            dialog.findViewById<EditText>(R.id.etPrDesc).setText(product.description)
            dialog.findViewById<EditText>(R.id.etPrPrice).setText(product.price)
            getPrImages(diImage, holder, product)
            dialog.show()

            dialog.findViewById<MaterialButton>(R.id.btnChoosePrImage).setOnClickListener {
                val i = Intent(Intent.ACTION_PICK)
                i.type = "image/*"
                SellerHome.myContext.startActivityForResult(i, 201)
            }
            dialog.findViewById<MaterialButton>(R.id.btnSubmitPr).setOnClickListener {
                updateProduct(product, position, diImage)
            }
        }


    }

    private fun updateProduct(product: ProductModel, position: Int, diImage: ImageView) {

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



    if (SellerHome.prImageUri != null) {
        var storageRef = storage.getReference()
        val ref = storageRef.child("${SellerHome.shopId}/product images/${product.name}.jpg")

        ref.delete()

        storageRef =
            storage.getReference().child("${SellerHome.shopId}/product images/${name}.jpg")
        storageRef.putFile(SellerHome.prImageUri!!).addOnCompleteListener{
            Toast.makeText(context,"product updated",Toast.LENGTH_LONG).show()
            loadingDialog.dismiss()
        }

    }else if (!name.equals(product.name)){

        val storageRef =
            storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name!!, "jpg")

        storageRef.getFile(localImage).addOnSuccessListener {
            Log.d("prUpdate", "new:  "+localImage.toUri())
            storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg").delete()

            val newRef = storage.getReference().child("${SellerHome.shopId}/product images/${name}.jpg")
            newRef.putFile(localImage.toUri()).addOnCompleteListener{
                Toast.makeText(context,"product updated",Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            }
        }

    }else{
        loadingDialog.dismiss()
    }

    }

    private fun getPrImages(diImage: ImageView?, holder: ViewHolder, product: ProductModel) {

        val storage: FirebaseStorage
        storage = FirebaseStorage.getInstance()

        val storageRef =
            storage.getReference().child("${SellerHome.shopId}/product images/${product.name}.jpg")
        val localImage = File.createTempFile(product.name!!, "jpg")
        Log.d("prUpdate", "updateProduct:  "+localImage.toUri())
        storageRef.getFile(localImage).addOnSuccessListener {
            imagBitmap = BitmapFactory.decodeFile(localImage.absolutePath)

            if (diImage == null) {
                holder.itemImage.setImageBitmap(imagBitmap)
            }else {
                diImage.setImageBitmap(imagBitmap)
            }
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
        val editIcon = itemView.findViewById<ImageView>(R.id.btnEditProduct)

    }

}