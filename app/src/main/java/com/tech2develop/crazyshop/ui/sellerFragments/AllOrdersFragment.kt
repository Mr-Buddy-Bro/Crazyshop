package com.tech2develop.crazyshop.ui.sellerFragments

import android.os.Bundle
import android.view.View
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.Adapters.AllOrdersAdapter
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.util.*
import kotlin.collections.ArrayList


class AllOrdersFragment : Fragment(R.layout.fragment_all_orders) {
    lateinit var ordersList: ArrayList<OrderModel>
    lateinit var spDuration: Spinner
    lateinit var spType: Spinner
    lateinit var firestore : FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ordersList = ArrayList()

        spDuration = view.findViewById(R.id.spDuration)
        spType = view.findViewById(R.id.spType)
        SellerHome.isDashboard = false
        firestore = FirebaseFirestore.getInstance()

        fetchOrders(view)

        view.findViewById<MaterialButton>(R.id.btnApplyFilter).setOnClickListener {
            fetchOrders(view)
        }

    }

    fun fetchOrders(view: View) {
        ordersList.clear()
        var duration = ""
        var type = ""
        when (spDuration.selectedItem.toString()) {
            "Today" -> duration = "Today"
            "Last 7 days" -> duration = "Last 7 days"
            "Last 30 days" -> duration = "Last 30 days"
            "Last 360 days" -> duration = "Last 360 days"
            "Lifetime" -> duration = "Lifetime"

        }
        when (spType.selectedItem.toString()) {
            "All" -> type = "ALL"
            "Un-delivered" -> type = "Un-delivered"
            "Delivered" -> type = "Delivered"
        }

        getOrders(view, duration, type)
    }

    private fun getOrders(view: View, duration: String, type: String) {
        val calendar = Calendar.getInstance().time
        val currentDate = calendar.date.toString()
        if (duration.equals("Today") && type.equals("Un-delivered")) {
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result!!) {
                            val orderDate = doc.data.getValue("date").toString()
                            val status = doc.data.getValue("deliveryStatus").toString()
                            val date = orderDate.substringBefore("/")
                            if (date.equals(currentDate) && type.equals(status)) {
                                var order = OrderModel(
                                    doc.data.getValue("itemName").toString(),
                                    null,
                                    doc.data.getValue("itemPrice").toString(),
                                    doc.data.getValue("deliveryStatus").toString(),
                                    doc.data.getValue("shopName").toString(),
                                    doc.data.getValue("date").toString())
                                ordersList.add(order)
                            }
                        }
                        setAdapter(view)
                    }
                }

        } else if (duration.equals("Last 7 days") && type.equals("Un-delivered")) {
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result!!) {
                            val orderDate = doc.data.getValue("date").toString()
                            val status = doc.data.getValue("deliveryStatus").toString()
                            val date = orderDate.substringBefore("/")
                            val monthstart = orderDate.substringAfter("/")
                            val month = monthstart.substringBefore("/").toInt()
                            var endDate = currentDate.toInt() - 7
                            var endMonth = 0
                            when (calendar.month) {
                                0 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                1 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                2 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 28 + endDate
                                    }
                                }
                                3 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                4 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }
                                5 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                6 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }
                                7 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                8 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                9 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }
                                10 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                11 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }

                            }
                            if (calendar.month + 1 == endMonth) {
                                if (date.toInt() <= currentDate.toInt() && date.toInt() >= endDate) {
                                    if (type.equals(status)) {
                                        var order = OrderModel(
                                            doc.data.getValue("itemName").toString(),
                                            null,
                                            doc.data.getValue("itemPrice").toString(),
                                            doc.data.getValue("deliveryStatus").toString()
                                        ,doc.data.getValue("shopName").toString(),
                                            doc.data.getValue("date").toString())
                                        ordersList.add(order)
                                    }
                                }
                            } else if (date.toInt() <= currentDate.toInt() && calendar.month + 1 >= month) {
                                if (type.equals(status)) {
                                    var order = OrderModel(
                                        doc.data.getValue("itemName").toString(),
                                        null,
                                        doc.data.getValue("itemPrice").toString(),
                                        doc.data.getValue("deliveryStatus").toString()
                                    ,doc.data.getValue("shopName").toString(),
                                        doc.data.getValue("date").toString())
                                    ordersList.add(order)
                                }
                            }
                        }
                        setAdapter(view)
                    }
                }
        } else if (duration.equals("Last 30 days") && type.equals("Un-delivered")) {
           firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result!!) {
                            val orderDate = doc.data.getValue("date").toString()
                            val status = doc.data.getValue("deliveryStatus").toString()
                            val monthstart = orderDate.substringAfter("/")
                            val month = monthstart.substringBefore("/").toInt()
                            if ( month <= (calendar.month + 1) && month > (calendar.month - 1)) {
                                    if (type.equals(status)) {
                                        var order = OrderModel(
                                            doc.data.getValue("itemName").toString(),
                                            null,
                                            doc.data.getValue("itemPrice").toString(),
                                            doc.data.getValue("deliveryStatus").toString()
                                        ,doc.data.getValue("shopName").toString(),
                                            doc.data.getValue("date").toString())
                                        ordersList.add(order)
                                    }
                            }
                        }
                        setAdapter(view)
                    }
                }
        } else if (duration.equals("Last 7 days") && type.equals("Delivered")) {
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (doc in it.result!!) {
                            val orderDate = doc.data.getValue("date").toString()
                            val status = doc.data.getValue("deliveryStatus").toString()
                            val date = orderDate.substringBefore("/")
                            val monthstart = orderDate.substringAfter("/")
                            val month = monthstart.substringBefore("/").toInt()
                            var endDate = currentDate.toInt() - 7
                            var endMonth = 0
                            when (calendar.month) {
                                0 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                1 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                2 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 28 + endDate
                                    }
                                }
                                3 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                4 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }
                                5 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                6 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }
                                7 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                8 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                9 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }
                                10 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 31 + endDate
                                    }
                                }
                                11 -> {
                                    if (calendar.date <= 7) {
                                        endMonth = calendar.month
                                        endDate = 30 + endDate
                                    }
                                }

                            }
                            if (calendar.month + 1 == endMonth) {
                                if (date.toInt() <= currentDate.toInt() && date.toInt() >= endDate) {
                                    if (type.equals(status)) {
                                        var order = OrderModel(
                                            doc.data.getValue("itemName").toString(),
                                            null,
                                            doc.data.getValue("itemPrice").toString(),
                                            doc.data.getValue("deliveryStatus").toString(),doc.data.getValue("shopName").toString(),
                                            doc.data.getValue("date").toString()
                                        )
                                        ordersList.add(order)
                                        break
                                    }
                                }
                            } else if (date.toInt() <= currentDate.toInt() && calendar.month + 1 >= month) {
                                if (type.equals(status)) {
                                    var order = OrderModel(
                                        doc.data.getValue("itemName").toString(),
                                        null,
                                        doc.data.getValue("itemPrice").toString(),
                                        doc.data.getValue("deliveryStatus").toString(),
                                        doc.data.getValue("shopName").toString(),
                                        doc.data.getValue("date").toString()
                                    )
                                    ordersList.add(order)
                                }
                            }
                        }
                        setAdapter(view)
                    }
                }
        }
    }

    private fun setAdapter(view: View) {
        val rvOrders = view.findViewById<RecyclerView>(R.id.rvAllOrders)
        val adapter = AllOrdersAdapter(view.context, ordersList)
        rvOrders.adapter = adapter
        rvOrders.layoutManager = LinearLayoutManager(view.context)
    }

}

