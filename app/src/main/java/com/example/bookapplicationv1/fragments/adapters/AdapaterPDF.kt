package com.example.bookapplicationv1.fragments.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.BookDetailsActivity
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.classes.ModelPDF
import com.example.bookapplicationv1.databinding.RowBooksBinding
import com.example.bookapplicationv1.fragments.adapters.AdapaterPDF.HolderPdfUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AdapaterPDF : RecyclerView.Adapter<HolderPdfUser>{

    // context, get using construtor
    private var context: Context

    //arraylist to hold pdfs
    private var pdfArrayList: ArrayList<ModelPDF>



    //viewbinding row_pdf_user.xml
    private lateinit var binding: RowBooksBinding



    //firebase
    private val mAuth = FirebaseAuth.getInstance()
    private val user: FirebaseUser? = mAuth.currentUser

    //final rate
    //rating
    private var totalRating = 0
    private var currentRating = 0F
    private var finalrating = 0F

    // construtor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPDF>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        // inflate layout
        binding = RowBooksBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        // get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val title = model.titulo
        val descricao = model.descricao
        val categoria = model.categoria
        val url = model.url
        val encrypted = model.livroEncriptacao
        val totalRating = model.totalreviews


        if (encrypted == "") {
            holder.moreOption.visibility = View.GONE
        }




        // set data
        holder.bookTitle.text = title
        holder.bookDesc.text = descricao
        holder.bookCateg.text = categoria
        holder.ratingtotal.text = totalRating.toString()


        //load book
        loadBookDetails(model, holder)
        holder.rating.rating = finalrating


        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        // handle click, open pdf details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", pdfId) // load respetive book
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // number of records
    }


    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val bookTitle = binding.bookTitle
        val bookDesc = binding.bookDesc
        val bookCateg = binding.bookCateg
        val moreOption = binding.moreOption
        val rating = binding.userRating
        val ratingtotal = binding.ratingtotal
    }

    private fun loadBookDetails(model: ModelPDF, holder: HolderPdfUser) {
        //Books > BookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(model.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val booktotalrating = "${snapshot.child("totalreviews").value}"
                    val bookcurrentraing = "${snapshot.child("atualreviews").value}"

                    totalRating = booktotalrating.toInt()
                    currentRating = bookcurrentraing.toFloat()

                    //final rating
                    finalrating = currentRating / totalRating

                    holder.rating.rating = finalrating

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }




}