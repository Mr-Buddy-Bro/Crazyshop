package com.tech2develop.crazyshop.ui.buyerFragments

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.TextureView
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.Adapters.WishListAdapter
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.Models.WishListModel
import com.tech2develop.crazyshop.R

class BuyerWishListFragment : Fragment(R.layout.fragment_buyer_wish_list) {

    lateinit var firestore : FirebaseFirestore
    lateinit var wishListArray : ArrayList<WishListModel>
    lateinit var loadingDialog : Dialog
    lateinit var refreshLayout : SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = false

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        refreshLayout = view.findViewById(R.id.btnRefreshWishList)
        refreshLayout.setOnRefreshListener {
            loadWishList(view)
        }

        wishListArray = ArrayList()

        loadWishList(view)

    }

    private fun loadWishList(view: View) {
        wishListArray.clear()
        loadingDialog.show()
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("Wish list")
            .get().addOnCompleteListener {
                refreshLayout.isRefreshing = false
                loadingDialog.dismiss()
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        val wishItem = WishListModel(doc.data.getValue("itemName").toString(),doc.data.getValue("itemDesc").toString(),
                            doc.data.getValue("itemPrice").toString(),doc.data.getValue("shopName").toString(),doc.data.getValue("shopKey").toString())
                        wishListArray.add(wishItem)
                    }
                    setAdapter(view)
                }
            }
    }

    private fun setAdapter(view: View) {
        if(wishListArray.isEmpty()){
            view.findViewById<TextView>(R.id.textView79).visibility = View.VISIBLE
        }else{
            view.findViewById<TextView>(R.id.textView79).visibility = View.INVISIBLE
        }
        val rv = view.findViewById<RecyclerView>(R.id.rvCart)
        val adapter = WishListAdapter(view.context, wishListArray)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(view.context)

    }

}