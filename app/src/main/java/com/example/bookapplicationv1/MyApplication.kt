package com.example.bookapplicationv1

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.HardwareRenderer
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookapplicationv1.classes.Constants
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNMAIL_TAG"


            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->


                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "Failed due to ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "Failed due to ${t.message}")
                        }
                        .onLoad { nbPages ->
                            progressBar.visibility = View.INVISIBLE

                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }.addOnFailureListener { e ->
                    Log.d(TAG, "Failed due to ${e.message}")
                }
        }

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            // use url to get size
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    val bytes = storageMetaData.sizeBytes.toDouble()

                    // convert bytes to kb/mb
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb >= 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "Failed due to ${e.message}")
                }

        }



        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String){
            // progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Um momento...")
            progressDialog.setMessage("Apagando o livro...")
            progressDialog.show()

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {

                    val ref = FirebaseDatabase.getInstance().getReference("Livros")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Apagado com sucesso", Toast.LENGTH_SHORT).show()
                        }.addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Apagado sem sucesso", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(context,"Não foi possivel apagar o livro", Toast.LENGTH_SHORT).show()
                }
        }
    }


}