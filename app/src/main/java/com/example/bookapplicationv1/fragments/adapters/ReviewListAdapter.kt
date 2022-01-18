package com.example.bookapplicationv1.fragments.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.BookDetailsActivity
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.classes.ModelComment
import com.example.bookapplicationv1.databinding.RowReviewsBinding
import com.example.bookapplicationv1.fragments.adapters.ReviewListAdapter.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception


class ReviewListAdapter : RecyclerView.Adapter<HolderReviewUser>  {

    // context, get using construtor
    private var context: Context

    //arraylist to hold pdfs
    private var reviewArrayList: ArrayList<ModelComment>

    //viewbinding RowReviewsBinding.xml
    private lateinit var binding: RowReviewsBinding

    // construtor
    constructor(context: Context, reviewArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.reviewArrayList = reviewArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderReviewUser {
        binding = RowReviewsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderReviewUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderReviewUser, position: Int) {
        val model = reviewArrayList[position]
        val bookId = model.bookId
        val rating = model.rating


        // set data
        holder.userRating.rating = rating.toFloat()

        // to get picture, load user details
        loadUserDetails(model, holder)

        // load book details
        loadBookDetails(model, holder)

        // handle click, open pdf details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", bookId) // load respetive book
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return reviewArrayList.size
    }

    // --- Load User Details
    private fun loadUserDetails(model: ModelComment, holder: ReviewListAdapter.HolderReviewUser) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("fullName").value}"
                    val profilePic = "${snapshot.child("userProfilePic").value}"

                    // set data
                    holder.userProfileName.text = name
                    // set image
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

    // --- Load Book Details
    private fun loadBookDetails(model: ModelComment, holder: ReviewListAdapter.HolderReviewUser) {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(model.bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookTitle = "${snapshot.child("titulo").value}"
                    val bookUrl = "${snapshot.child("url").value}"

                    //set data
                    holder.bookTitle.text = bookTitle

                    //load pdf
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        holder.pdfView,
                        holder.progressBar,
                        null
                    )
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }



    inner class HolderReviewUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val bookTitle = binding.bookTitle
        val profilePic = binding.profilePic
        val userProfileName = binding.userProfileName
        val userRating = binding.userRating
    }

}