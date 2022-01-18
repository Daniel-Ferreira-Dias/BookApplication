package com.example.bookapplicationv1.fragments.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.classes.ModelComment
import com.example.bookapplicationv1.databinding.RowCommentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment> {
    // context
    val context: Context

    // arraylist to hold comments
    val commentArrayList: ArrayList<ModelComment>

    // view binding
    private lateinit var binding: RowCommentsBinding

    //firebase
    private val mAuth = FirebaseAuth.getInstance()

    var novoRating = 0
    var novoAtualRating = 0F

    // constructor
    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        // get data
        val model = commentArrayList[position]
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp
        val rating = model.rating

        // format time stamp
        val date = MyApplication.formatTimeStamp(timestamp.toLong())

        // set data
        holder.commentTimeStamp.text = date
        holder.userComment.text = comment
        holder.userRating.rating = rating.toFloat()

        // to get picture, load user details
        loadUserDetails(model, holder)

        // show dialog to delete comment
        holder.itemView.setOnClickListener {
            if (mAuth.currentUser == null) {
                Toast.makeText(context, "Tem que fazer o login !", Toast.LENGTH_SHORT).show()
            } else if (mAuth.uid != uid) {
                Toast.makeText(
                    context,
                    "Não pode apagar comentários de outros utilizadores.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                deleteCommentDialog(model, holder)
            }
        }

    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Apagar comentário")
            .setMessage("Tem a certeza que quer apagar este comentário?")
            .setPositiveButton("Apagar") { d, e ->
                val bookId = model.bookId
                val commentId = model.id

                val auxref = FirebaseDatabase.getInstance().getReference("Livros")
                auxref.child(bookId)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val booktotalrating = "${snapshot.child("totalreviews").value}".toInt()
                            val bookcurrentraing = "${snapshot.child("atualreviews").value}"

                            novoRating = booktotalrating - 1
                            novoAtualRating = bookcurrentraing.toFloat() - model.rating.toFloat()

                            //update
                            val auxsecref = FirebaseDatabase.getInstance().getReference("Livros")
                            auxsecref.child(bookId).child("atualreviews").setValue(novoAtualRating)
                            auxsecref.child(bookId).child("totalreviews").setValue(novoRating)

                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

                val ref = FirebaseDatabase.getInstance().getReference("Livros")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Comentário apagado com sucesso !",
                            Toast.LENGTH_SHORT
                        ).show()

                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Não foi possivel apagar o comentário devido ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                val mfRef = FirebaseDatabase.getInstance().getReference("Comentários")
                mfRef.child(commentId)
                    .removeValue()

            }.setNegativeButton("Cancelar") { d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("fullName").value}"
                    val profilePic = "${snapshot.child("userProfilePic").value}"

                    // set data
                    holder.userProfileName.text = name
                    try {
                        Glide.with(context)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_user_default)
                            .into(holder.profilePic)
                    } catch (e: Exception) {
                        holder.profilePic.setImageResource(R.drawable.ic_user_default)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class HolderComment(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePic = binding.profilePic
        val userProfileName = binding.userProfileName
        val commentTimeStamp = binding.commentTimeStamp
        val userComment = binding.userComment
        val userRating = binding.userRating
    }


}