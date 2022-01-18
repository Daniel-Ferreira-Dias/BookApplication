package com.example.bookapplicationv1.fragments.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.BookDetailsActivity
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.classes.ModelPDF
import com.example.bookapplicationv1.databinding.RowFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class adapterFavorite : RecyclerView.Adapter<adapterFavorite.HolderFavorite>{

    // context, get using construtor
    private var context: Context

    //arraylist to hold pdfs
    private var favoriteArrayList: ArrayList<ModelPDF>

    //view binding
    private lateinit var binding: RowFavoritesBinding

    //firebase
    private val mAuth = FirebaseAuth.getInstance()
    private val user: FirebaseUser? = mAuth.currentUser

    //construtor
    constructor(context: Context, favoriteArrayList: java.util.ArrayList<ModelPDF>) {
        this.context = context
        this.favoriteArrayList = favoriteArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFavorite {
        binding = RowFavoritesBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderFavorite, position: Int) {
        // get data
        val model = favoriteArrayList[position]
        val bookid = model.id

        loadBookDetails(model, holder)

        checkUser(model, holder)

        //holder item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", bookid) // load respetive book
            context.startActivity(intent)
        }

        //handle click, remove from favorite
        binding.favoriteImage.setOnClickListener {
            removeFromFavorite(model, holder)
        }

    }

    private fun checkUser(model: ModelPDF, holder: HolderFavorite) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user!!.uid).child("Favoritos")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uid = "${snapshot.child("uid").value}"

                    if (user.uid != uid){
                        holder.favoriteImage.visibility = View.INVISIBLE
                    }else if (user.uid == uid){
                        holder.favoriteImage.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadBookDetails(model: ModelPDF, holder: adapterFavorite.HolderFavorite) {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(model.id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val title = "${snapshot.child("titulo").value}"
                    val url = "${snapshot.child("url").value}"

                    //set data to model
                    model.isFavorite = true
                    model.titulo = title

                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    holder.bookTitle.text = title
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    override fun getItemCount(): Int {
        return favoriteArrayList.size
    }

    inner class HolderFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var favoriteImage = binding.favoriteImage
        var bookTitle = binding.bookTitle
    }

    private fun removeFromFavorite(model: ModelPDF, holder: adapterFavorite.HolderFavorite){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favoritos").child(model.id)
            .removeValue().addOnSuccessListener {
                Toast.makeText(context, "Removido dos favoritos!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e->
                Toast.makeText(context, "Não foi possivel remover dos favoritos devida a a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}