package com.example.bookapplicationv1.dialogs

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.EditProfileActivity
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.databinding.FragmentDialogCommentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_book_details.*
import kotlinx.android.synthetic.main.fragment_dialog_changepassword.*
import kotlinx.android.synthetic.main.fragment_dialog_comment.*
import kotlinx.android.synthetic.main.fragment_dialog_comment.profilePic
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception

class CustomCommentFragment : BottomSheetDialogFragment() {

    var bookId = ""
    //comment
    private var comment = ""
    private var bookRating = ""

    //rating
    private var totalRating = 0
    private var currentRating = 0F
    private var finalrating = 0F

    // firebase auth
    private val mAuth = FirebaseAuth.getInstance()
    private val user: FirebaseUser? = mAuth.currentUser

    // dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_comment, container, false)


        //progress bar
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments
        bookId = data!!.get("bookId").toString()

        loadBookDetails()

        loadUser()

        submitComment.setOnClickListener {
            comment = reviewTextField.text.toString().trim()
            bookRating = ratingStars.rating.toString()

            if (comment.isEmpty() || bookRating.isEmpty()){
                Toast.makeText(requireContext(), "Insira os campos necessários", Toast.LENGTH_SHORT).show()
            }else{
                addComment()
            }
        }

    }

    private fun loadUser() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImage = "${snapshot.child("userProfilePic").value}"
                    //set image
                    try {
                        Glide.with(this@CustomCommentFragment).load(profileImage)
                            .placeholder(R.drawable.usericon).into(profilePic)
                    } catch (e: Exception) {
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadBookDetails() {
        //Books > BookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val booktotalrating = "${snapshot.child("totalreviews").value}"
                    val bookcurrentraing = "${snapshot.child("atualreviews").value}"
                    totalRating = booktotalrating.toInt()
                    currentRating = bookcurrentraing.toFloat()
                    //final rating
                    finalrating = currentRating / totalRating
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addComment() {
        progressDialog.setMessage("Adicionando o comentário...")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        // set up data to add in db for comment
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${mAuth.uid}"
        hashMap["rating"] = "$bookRating"


        val mref = FirebaseDatabase.getInstance().getReference("Comentários")
        mref.child(timestamp)
            .setValue(hashMap)

        // sum one totalrating and current rating and update in books
        totalRating += 1
        currentRating += bookRating.toFloat()

        val secref = FirebaseDatabase.getInstance().getReference("Livros")
        secref.child(bookId).child("atualreviews").setValue(currentRating)
        secref.child(bookId).child("totalreviews").setValue(totalRating)

        // Db path to add into it
        // Books > BooksId >Comments > CommentsId > CommentData
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Comentário adicionado!", Toast.LENGTH_SHORT).show()
                dismiss()
            }.addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Não foi possivel adicionar comentário devido a ${e.message}", Toast.LENGTH_SHORT).show()
                dismiss()
            }
    }

}