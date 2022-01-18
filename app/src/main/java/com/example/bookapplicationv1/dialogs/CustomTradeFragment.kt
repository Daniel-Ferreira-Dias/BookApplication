package com.example.bookapplicationv1.dialogs

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.classes.ModelPDF
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_custom_trade.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception

class CustomTradeFragment : BottomSheetDialogFragment() {

    private lateinit var progressDialog : ProgressDialog

    //array to hold books
    private lateinit var bookArrayList : ArrayList<ModelPDF>
    private lateinit var otherbookArrayList : ArrayList<ModelPDF>

    // firebase auth
    private val mAuth = FirebaseAuth.getInstance()


    //private var
    var recieverUID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_custom_trade, container, false)

        //set up progress dialog
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Um momento")
        progressDialog.setCanceledOnTouchOutside(false)

        val data = arguments
        recieverUID = data!!.get("recieverUID").toString()

        loadBookCurrentUser()
        loadcurrentUserPic()
        loadcurrentOtherUserPic()


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments
        recieverUID = data!!.get("recieverUID").toString()

        //handle click pick book
        currentUserPDF.setOnClickListener {
            categoryPickUpDialog()
        }

        otherUserPdf.setOnClickListener {
            categoryOtherPickUpDialog()
        }

        //handle send trade
        enviarTrade.setOnClickListener {
            sendTrade()
        }

    }

    private fun loadBookCurrentUser(){
        bookArrayList = ArrayList()
        otherbookArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                bookArrayList.clear()
                otherbookArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPDF::class.java)
                    if (model!!.uid == mAuth.uid && model.livroEncriptacao != "") {
                        bookArrayList.add(model!!)
                    }
                    if (model!!.uid == recieverUID && model.livroEncriptacao != ""){
                        otherbookArrayList.add(model!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private var currentUserselectedBookId = ""
    private var currentUserselectedTitle = ""

    private fun categoryPickUpDialog(){
        val bookArray = arrayOfNulls<String>(bookArrayList.size)
        for (i in bookArrayList.indices){
            bookArray[i] = bookArrayList[i].titulo
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Escolha um dos seus livros")
            .setItems(bookArray){ dialog, which->
                currentUserselectedTitle = bookArrayList[which].titulo
                currentUserselectedBookId = bookArrayList[which].id

                currentUserPDF.text = currentUserselectedTitle
            }.show()
    }

    private var otherUserselectedBookId = ""
    private var otherUserselectedTitle = ""

    private fun categoryOtherPickUpDialog(){
        val otherbookArray = arrayOfNulls<String>(otherbookArrayList.size)
        for (i in otherbookArrayList.indices){
            otherbookArray[i] = otherbookArrayList[i].titulo
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Escolhe o livro do outro utilizador")
            .setItems(otherbookArray){ dialog, e->
                otherUserselectedTitle = otherbookArrayList[e].titulo
                otherUserselectedBookId = otherbookArrayList[e].id

                otherUserPdf.text = otherUserselectedTitle
            }.show()

    }

    private fun loadcurrentUserPic() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImage = "${snapshot.child("userProfilePic").value}"

                    try {
                        Glide.with(requireContext()).load(profileImage)
                            .placeholder(R.drawable.usericon).into(currentUserProfilePic)
                    } catch (e: Exception) {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadcurrentOtherUserPic() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(recieverUID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImage = "${snapshot.child("userProfilePic").value}"
                    try {
                        Glide.with(requireContext()).load(profileImage)
                            .placeholder(R.drawable.usericon).into(otherUserProfilePic)
                    } catch (e: Exception) {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun sendTrade() {
        progressDialog.setMessage("Enviando a trade")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        // set up data to add in db for comment
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["tradeOwner"] = mAuth.uid.toString()
        hashMap["tradeReciever"] = recieverUID
        hashMap["currentUserselectedBookId"] = currentUserselectedBookId
        hashMap["otherUserselectedBookId"] = otherUserselectedBookId
        hashMap["status"] = 1

        val mref = FirebaseDatabase.getInstance().getReference("Users")
        mref.child(mAuth.uid!!).child("Trade").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Trade enviando com sucesso!", Toast.LENGTH_SHORT).show()
                dismiss()
            }.addOnFailureListener { e->
                Toast.makeText(requireContext(), "Erro no envio da trade devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

        val dref = FirebaseDatabase.getInstance().getReference("Users")
        dref.child(recieverUID).child("Trade").child(timestamp)
            .setValue(hashMap)
    }

}