package com.tech2develop.crazyshop.ui.buyerFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.Adapters.MyOrdersAdapter
import com.tech2develop.crazyshop.BuyerHome
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R

class BuyerMyOrdersFragment : Fragment(R.layout.fragment_buyer_my_orders) {

    lateinit var myView : View
    lateinit var firestore : FirebaseFirestore
    lateinit var list : ArrayList<OrderModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BuyerHome.isHome = false

        myView = view
        firestore = FirebaseFirestore.getInstance()
        list = ArrayList()

        getMyOrders()

    }

    fun getMyOrders() {
        firestore.collection("Buyer").document(BuyerHome.auth.currentUser?.email!!).collection("All orders").get()
            .addOnCompleteListener {
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
        val rv = myView.findViewById<RecyclerView>(R.id.rvMyOrder)
        rv.adapter = MyOrdersAdapter(myView.context, list)
        rv.layoutManager = LinearLayoutManager(myView.context)
    }

}