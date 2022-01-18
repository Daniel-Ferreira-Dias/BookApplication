package com.example.bookapplicationv1.fragments.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.BookDetailsActivity
import com.example.bookapplicationv1.EditBookActivity
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.classes.ModelPDF
import com.example.bookapplicationv1.databinding.RowBooksBinding
import com.example.bookapplicationv1.databinding.RowMycollectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AdaptarCollection : RecyclerView.Adapter<AdaptarCollection.HolderCollection> {

    // context, get using construtor
    private var context: Context

    //arraylist to hold pdfs
    private var collectionArrayList: ArrayList<ModelPDF>


    //viewbinding row_pdf_user.xml
    private lateinit var binding: RowMycollectionBinding

    //firebase
    private val mAuth = FirebaseAuth.getInstance()
    private val user: FirebaseUser? = mAuth.currentUser

    // construtor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPDF>) {
        this.context = context
        this.collectionArrayList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCollection {
        // inflate layout
        binding = RowMycollectionBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCollection(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCollection, position: Int) {
        val model = collectionArrayList[position]
        val pdfId = model.id
        val title = model.titulo
        val url = model.url
        val uid = model.uid

        // set data
        holder.bookTitle.text = title

        if (user!!.uid != uid){
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
        }
        // edit book
        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditBookActivity::class.java)
            intent.putExtra("bookId", pdfId) // load respetive book
            context.startActivity(intent)
        }

        // handle click, open pdf details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", pdfId) // load respetive book
            context.startActivity(intent)
        }

        // delete book
        holder.deleteButton.setOnClickListener {
        }

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfpic, holder.progressBar, null)
    }

    override fun getItemCount(): Int {
        return collectionArrayList.size
    }


    inner class HolderCollection(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle = binding.bookTitle
        val pdfpic = binding.pdfView
        val progressBar = binding.progressBar
        val editButton = binding.editBook
        val deleteButton = binding.deleteBook
    }

}