package com.tech2develop.crazyshop.ui.sellerFragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.tech2develop.crazyshop.Models.OrderModel
import com.tech2develop.crazyshop.Models.SellerModel
import com.tech2develop.crazyshop.Models.VerificationReqModel
import com.tech2develop.crazyshop.R
import com.tech2develop.crazyshop.SellerHome
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    lateinit var firestore : FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storage : FirebaseStorage
    lateinit var user: FirebaseUser
    lateinit var progress : ProgressDialog
    var docUri : Uri? = null
    companion object{
        lateinit var isVerified: String
    }
    lateinit var btnVerify1 : MaterialButton
    lateinit var chooseDoc : MaterialButton
    lateinit var submitVerify : MaterialButton
    lateinit var btnVerifyEmail : TextView
    lateinit var dialog : Dialog
    val CHOOSE_DOC_REQ_CODE = 102
    lateinit var pop_up_view : View

    lateinit var tvDCR : TextView
    lateinit var tvDashTodayAllOrders : TextView
    lateinit var tvDashDelivered : TextView
    lateinit var tvDashUnDelivered : TextView
    lateinit var refreshLay : SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SellerHome.isDashboard = true

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        tvDCR = view.findViewById(R.id.textView25)
        tvDashTodayAllOrders = view.findViewById(R.id.tvDashTodayAllOrders)
        tvDashDelivered = view.findViewById(R.id.tvDashDelivered)
        tvDashUnDelivered = view.findViewById(R.id.tvDashUnDelivered)
        refreshLay = view.findViewById(R.id.refreshLay)
        refreshLay.setOnRefreshListener {
            getDashboardData(view)
//            checkVerified(view)
        }
//        refreshLay.setOn

        getDashboardData(view)

        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.varify_popup)
        btnVerify1 = view.findViewById(R.id.btnVerify1)
        btnVerifyEmail = dialog.findViewById(R.id.btnVerifyEmail)

        dialog.findViewById<TextView>(R.id.textView65).text = auth.currentUser!!.email

        btnVerify1.setOnClickListener {
            btnPopVarify(view)
            checkEmailVerified()
        }
        chooseDoc = dialog.findViewById(R.id.btnChooseDoc)
        chooseDoc.setOnClickListener {
            chooseDocu(view)
        }
        submitVerify = dialog.findViewById(R.id.materialButton5)
        submitVerify.setOnClickListener {
           if(auth.currentUser!!.isEmailVerified){
               dialog.findViewById<TextView>(R.id.textView66).visibility = View.INVISIBLE
               if (docUri != null) {
                   submitDoc(view)
               }else{
                   Toast.makeText(view.context, "Please choose a document", Toast.LENGTH_SHORT).show()
               }
           }else{
               dialog.findViewById<TextView>(R.id.textView66).visibility = View.VISIBLE
           }
        }

        btnVerifyEmail.setOnClickListener {
            if (auth.currentUser!!.isEmailVerified){
                Toast.makeText(view.context, "Email already verified", Toast.LENGTH_SHORT).show()
            }else{
                auth.currentUser!!.sendEmailVerification().addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(view.context, "Verification link send to ${auth.currentUser!!.email}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        progress = ProgressDialog(view.context)
        progress.setTitle("Loading..")
        progress.setMessage("please wait")

        checkVerified(view)

    }

    private fun getDashboardData(view: View) {
        val list = ArrayList<OrderModel>()
        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(
            Calendar.YEAR)}"
        firestore.collection("Seller").document(SellerHome.auth.currentUser?.email!!).collection("All orders")
            .get().addOnCompleteListener {
                refreshLay.isRefreshing = false
                if (it.isSuccessful){
                    for (doc in it.result){
                        if (doc.data.getValue("date").toString().equals(currentDate)){
                            val order = OrderModel(
                                doc.data.getValue("itemName").toString(),
                                null,
                                doc.data.getValue("itemPrice").toString(),
                                doc.data.getValue("deliveryStatus").toString(),
                                doc.data.getValue("shopName").toString(),
                                doc.data.getValue("date").toString(), null, null)
                            list.add(order)
                        }
                    }
                    setData(list)
                }
            }
    }

    private fun setData(list: ArrayList<OrderModel>) {

        tvDashTodayAllOrders.text = list.size.toString()

        val deliveredList = ArrayList<String>()

        for (doc in list){
            if (doc.deliveryStatus!!.equals("Delivered")){
                deliveredList.add(doc.itemName.toString())
            }
        }
        con1(list, deliveredList)

    }

    private fun con1(list: ArrayList<OrderModel>, deliveredList: ArrayList<String>) {
        Log.d("dash", list.size.toString() + " "+ deliveredList.size.toString())
        val unDeliveredList = ArrayList<String>()
        for (doc in list){
            if (doc.deliveryStatus!!.equals("Un-delivered")){
                unDeliveredList.add(doc.itemName.toString())
            }
        }
        makeIt(unDeliveredList.size, deliveredList.size, list.size)
    }

    private fun makeIt(size: Int, size1: Int, size2: Int) {
        tvDashDelivered.text = size1.toString()
        tvDashUnDelivered.text = size.toString()
        val DCR = (((size1.toDouble() / size2)*100).toString()).substringBefore(".")+"%"
        if (size > 0) {
            tvDCR.text = DCR
        }else{
            tvDCR.text = "0%"
        }
    }

    @SuppressLint("ResourceAsColor")
    fun checkEmailVerified(){
        auth.currentUser!!.reload().addOnCompleteListener {
            Log.d("TAG", "onResume: Checking email ${auth.currentUser!!.isEmailVerified}")
            if (auth.currentUser!!.isEmailVerified){
                btnVerifyEmail.text = "Verified"
                dialog.findViewById<TextView>(R.id.textView66).visibility = View.INVISIBLE
                btnVerifyEmail.setTextColor(R.color.teal_700)
            }
        }
    }

    fun submitDoc(view: View) {
        progress.setTitle("Submitting")
        progress.show()
        var seller : SellerModel
        firestore.collection("Seller").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    if (doc.id == user.email){
                        seller = SellerModel(doc.data.getValue("companyName").toString(),doc.data.getValue("companyDescription").toString(),
                            doc.data.getValue("fullName").toString(),
                            doc.data.getValue("email").toString(),doc.data.getValue("phoneNo").toString(),doc.data.getValue("password").toString(),
                            doc.data.getValue("audienceSize").toString(),"progress",doc.data.getValue("sellerKey").toString())
                        firestore.collection("Seller").document(user.email!!).set(seller).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful){
                                val storageRef = storage.getReference("${seller.sellerKey}/govId")
                                storageRef.putFile(docUri!!).addOnCompleteListener{
                                    val verificationReqModel = VerificationReqModel(seller.email, seller.sellerKey)
                                    firestore.collection("Verification requests").add(verificationReqModel).addOnCompleteListener {
                                        progress.dismiss()
                                        dialog.dismiss()
                                        makeInVerifyProgress(view)
                                    }

                                }

                            }else{
                                dialog.dismiss()
                            }
                        }

                        break
                    }
                }
            }
        }
    }

    fun makeInVerifyProgress(view: View) {
        view.findViewById<TextView>(R.id.textView12).text = "Verification in progress"
        view.findViewById<TextView>(R.id.textView13).text = "Your ID is being verifies. It takes 1-3 days to verify your Identity"
        view.findViewById<ImageView>(R.id.ivProgress).visibility = View.VISIBLE
        view.findViewById<MaterialButton>(R.id.btnVerify1).visibility = View.INVISIBLE

        progress.dismiss()
        view.findViewById<LinearLayout>(R.id.verifyCard).visibility = View.VISIBLE
    }

    fun chooseDocu(view: View) {
        val i = Intent(Intent.ACTION_PICK)
        i.type = "image/*"
        pop_up_view = view
        startActivityForResult(i, CHOOSE_DOC_REQ_CODE)
    }

    fun btnPopVarify(view: View) {
        dialog.show()
    }

    private fun checkVerified(view: View) {
        progress.show()
        user = auth.currentUser!!
        firestore.collection("Seller").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (document in it.result!!){
                    if (document.id == user?.email){
                        isVerified = document.data.getValue("verified").toString()
                        if (isVerified.equals("false")){
                            showVerify(view)
                        }else if (isVerified.equals("progress")){
                            makeInVerifyProgress(view)
                        }else if (isVerified.equals("true")){
                            continueProcess(view)
                        }

                        break
                    }
                }
            }
        }
    }

    private fun continueProcess(view: View) {
        progress.dismiss()
        view.findViewById<LinearLayout>(R.id.dashboardView).visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_DOC_REQ_CODE && resultCode == AppCompatActivity.RESULT_OK){
            docUri = data?.data
            dialog.findViewById<ImageView>(R.id.ivDoc).setImageURI(docUri)
            dialog.findViewById<ImageView>(R.id.ivDoc).visibility = View.VISIBLE
            dialog.findViewById<TextView>(R.id.tvNoteVar).visibility = View.INVISIBLE
        }
    }

    private fun showVerify(view: View) {
        progress.dismiss()
        view.findViewById<LinearLayout>(R.id.verifyCard).visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        checkEmailVerified()
    }
}