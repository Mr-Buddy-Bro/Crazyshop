package com.tech2develop.crazyshop.ui.buyerFragments

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.Adapters.MyOrdersAdapter
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R

class BuyerMyOrdersFragment : Fragment(R.layout.fragment_buyer_my_orders) {

    lateinit var myView : View
    lateinit var firestore : FirebaseFirestore
    lateinit var list : ArrayList<OrderModel>
    lateinit var refreshLay : SwipeRefreshLayout
    lateinit var loadingDialog : Dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = false

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        myView = view
        firestore = FirebaseFirestore.getInstance()
        list = ArrayList()

        getMyOrders()

        refreshLay = view.findViewById(R.id.refreshBuyerAllOrders)
        refreshLay.setOnRefreshListener {
            getMyOrders()
        }
    }

    fun getMyOrders() {
        list.clear()
        loadingDialog.show()
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("All orders").get()
            .addOnCompleteListener {
                loadingDialog.dismiss()
                refreshLay.isRefreshing = false
                if (it.isSuccessful){
                    for (doc in it.result!!){
                        val item = OrderModel(doc.data.getValue("itemName").toString(), null, doc.data.getValue("itemPrice").toString(),
                            doc.data.getValue("deliveryStatus").toString(),doc.data.getValue("shopName").toString(),doc.data.getValue("date").toString(),
                            doc.data.getValue("shopKey").toString(), doc.id)
                        list.add(item)
                    }
                    setAdapter()
                }
            }
    }

    private fun setAdapter() {

        if (list.isEmpty()){
            myView.findViewById<TextView>(R.id.tvNoOrdersBuyer).visibility = View.VISIBLE
        }else{
            myView.findViewById<TextView>(R.id.tvNoOrdersBuyer).visibility = View.INVISIBLE
        }

        val rv = myView.findViewById<RecyclerView>(R.id.rvMyOrder)
        rv.adapter = MyOrdersAdapter(myView.context, list)
        rv.layoutManager = LinearLayoutManager(myView.context)
    }

}