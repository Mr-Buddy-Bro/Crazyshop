package com.tech2develop.crazyshop.ui.sellerFragments

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.tech2develop.crazyshop.Models.FeedbackModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.util.*

class Help_and_feedbackFragment: Fragment(R.layout.fragment_help) {

    lateinit var etFeedMsg : EditText
    lateinit var loadingDialog : Dialog
    lateinit var firebase : FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = Dialog(view.context)
        loadingDialog.setContentView(R.layout.loading_layout)
        loadingDialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        firebase = FirebaseFirestore.getInstance()

        etFeedMsg = view.findViewById(R.id.editText)
        view.findViewById<MaterialButton>(R.id.sendFeedback).setOnClickListener {
            if (etFeedMsg.text.isEmpty()){
                Toast.makeText(view.context, "Please type your feedback", Toast.LENGTH_LONG).show()
            }else{
                submitFeedback(view, etFeedMsg.text.toString())
            }
        }
    }

    private fun submitFeedback(view: View, feed: String) {

        loadingDialog.show()
        val calendar = Calendar.getInstance().time
        val date = calendar.date
        val month = calendar.month+1
        val year = calendar.year
        val currentDate = "$date/$month/$year"
        val feedback = FeedbackModel(currentDate, SellerHome.auth.currentUser?.email, "Seller", feed)
        firebase.collection("Feedbacks").add(feedback).addOnCompleteListener {
            loadingDialog.dismiss()
            if (it.isSuccessful){
                Toast.makeText(view.context, "Feedback received. Thank you for your feedback", Toast.LENGTH_LONG).show()
                etFeedMsg.setText("")
            }else{
                Toast.makeText(view.context, "Failed to send feedback. Please try again later", Toast.LENGTH_LONG).show()
            }
        }

    }

}