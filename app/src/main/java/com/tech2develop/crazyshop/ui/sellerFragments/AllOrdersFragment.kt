package com.tech2develop.crazyshop.ui.sellerFragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
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
import javax.xml.datatype.DatatypeConstants.MONTHS
import kotlin.collections.ArrayList


class AllOrdersFragment : Fragment(R.layout.fragment_all_orders) {
    lateinit var ordersList: ArrayList<OrderModel>
    lateinit var spDuration: Spinner
    lateinit var spType: Spinner
    lateinit var firestore : FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.N)
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

    @RequiresApi(Build.VERSION_CODES.N)
    fun fetchOrders(view: View) {
        ordersList.clear()
        var duration = ""
        var type = ""
        when (spDuration.selectedItem.toString()) {
            "Today" -> duration = "Today"
            "Custom" -> duration = "Custom"
            "Lifetime" -> duration = "Lifetime"

        }
        when (spType.selectedItem.toString()) {
            "All" -> type = "ALL"
            "Un-delivered" -> type = "Un-delivered"
            "Delivered" -> type = "Delivered"
        }

        getOrders(view, duration, type)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getOrders(view: View, duration: String, type: String) {
        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(Calendar.YEAR)}"

        if (duration.equals("Today")) {
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        ordersList.clear()
                        for (doc in it.result!!) {
                            val orderDate = doc.data.getValue("date").toString()
                            val status = doc.data.getValue("deliveryStatus").toString()

                            if (orderDate.equals(currentDate) && (type.equals(status) || type.equals("ALL"))) {
                                val order = OrderModel(
                                    doc.data.getValue("itemName").toString(),
                                    null,
                                    doc.data.getValue("itemPrice").toString(),
                                    doc.data.getValue("deliveryStatus").toString(),
                                    doc.data.getValue("shopName").toString(),
                                    doc.data.getValue("date").toString(), null, null)
                                ordersList.add(order)
                            }
                        }
                        setAdapter(view)
                    }
                }
        }
        else if (duration.equals("Lifetime")) {
            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        ordersList.clear()
                        for (doc in it.result!!) {
                            val status = doc.data.getValue("deliveryStatus").toString()

                            if ((type.equals(status) || type.equals("ALL"))) {
                                val order = OrderModel(
                                    doc.data.getValue("itemName").toString(),
                                    null,
                                    doc.data.getValue("itemPrice").toString(),
                                    doc.data.getValue("deliveryStatus").toString(),
                                    doc.data.getValue("shopName").toString(),
                                    doc.data.getValue("date").toString(), null, null)
                                ordersList.add(order)
                            }
                        }
                        setAdapter(view)
                    }
                }
        }
        else if (duration.equals("Custom")) {

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(view.context, DatePickerDialog.OnDateSetListener { view1, year1, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                Log.d("TAG", "$dayOfMonth/${monthOfYear+1}/$year")
                val choosenDate = "$dayOfMonth/${monthOfYear+1}/$year1"

            firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!)
                .collection("All orders").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        ordersList.clear()
                        for (doc in it.result!!) {
                            val status = doc.data.getValue("deliveryStatus").toString()
                            val orderDate = doc.data.getValue("date").toString()

                            if ((type.equals(status) || type.equals("ALL")) && orderDate.equals(choosenDate)) {
                                val order = OrderModel(
                                    doc.data.getValue("itemName").toString(),
                                    null,
                                    doc.data.getValue("itemPrice").toString(),
                                    doc.data.getValue("deliveryStatus").toString(),
                                    doc.data.getValue("shopName").toString(),
                                    doc.data.getValue("date").toString(), null, null)
                                ordersList.add(order)
                            }
                        }
                        setAdapter(view)
                    }
                }

            }, year, month, day)
            dpd.show()
        }
    }

    private fun setAdapter(view: View) {
        if (ordersList.isEmpty()){
            view.findViewById<TextView>(R.id.tvNoOrders).visibility = View.VISIBLE
        }else{
            view.findViewById<TextView>(R.id.tvNoOrders).visibility = View.GONE
        }
        val rvOrders = view.findViewById<RecyclerView>(R.id.rvAllOrders)
        val adapter = AllOrdersAdapter(view.context, ordersList)
        rvOrders.adapter = adapter
        rvOrders.layoutManager = LinearLayoutManager(view.context)
    }

}

