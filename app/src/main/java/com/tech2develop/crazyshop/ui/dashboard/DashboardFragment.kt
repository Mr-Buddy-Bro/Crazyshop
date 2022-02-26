package com.tech2develop.crazyshop.ui.dashboard

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tech2develop.crazyshop.Models.SellerModel
import com.tech2develop.crazyshop.Models.VerificationReqModel
import com.tech2develop.crazyshop.R

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
    lateinit var dialog : Dialog
    val CHOOSE_DOC_REQ_CODE = 102
    lateinit var pop_up_view : View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = Dialog(view.context)
        dialog.setContentView(R.layout.varify_popup)
        btnVerify1 = view.findViewById(R.id.btnVerify1)
        btnVerify1.setOnClickListener {
            btnPopVarify(view)
        }
        chooseDoc = dialog.findViewById(R.id.btnChooseDoc)
        chooseDoc.setOnClickListener {
            chooseDocu(view)
        }
        submitVerify = dialog.findViewById(R.id.materialButton5)
        submitVerify.setOnClickListener {
            submitDoc(view)
        }

        progress = ProgressDialog(view.context)
        progress.setTitle("Loading..")
        progress.setMessage("please wait")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        checkVerified(view)


    }

    fun submitDoc(view: View) {
        progress.setTitle("Submitting")
        progress.show()
        var seller : SellerModel
        firestore.collection("Seller").get().addOnCompleteListener {
            if (it.isSuccessful){
                for (doc in it.result!!){
                    if (doc.id == user?.email){
                        seller = SellerModel(doc.data.getValue("companyName").toString(),doc.data.getValue("companyDescription").toString(),
                            doc.data.getValue("fullName").toString(),
                            doc.data.getValue("email").toString(),doc.data.getValue("phoneNo").toString(),doc.data.getValue("password").toString(),
                            doc.data.getValue("audienceSize").toString(),"progress",doc.data.getValue("sellerKey").toString())
                        firestore.collection("Seller").document(user?.email!!).set(seller).addOnCompleteListener { it1 ->
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
        var i = Intent(Intent.ACTION_PICK)
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
        view.findViewById<ConstraintLayout>(R.id.dashboardView).visibility = View.VISIBLE
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
}