package com.example.bookapplicationv1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bookapplicationv1.databinding.ActivityPdfViewBinding
import com.example.bookapplicationv1.classes.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityPdfViewBinding

    //book id
    var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id
        bookId = intent.getStringExtra("bookId")!!

        //loadbookdetails
        loadBookDetails()

        //handle go back
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        // get id from database
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book url
                    val pdfUrl = snapshot.child("url").value

                    //load pdf from url
                    loadBookFromUrl("$pdfUrl")

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(true)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.pageBook.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this,
                            "Falha na visualização do pdf devido a ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onPageError { page, t ->
                        Toast.makeText(
                            this,
                            "Falha na visualização da página devido a ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility= View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Falha na visualização do pdf devido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility= View.GONE
            }
    }
}