package com.example.bookapplicationv1

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapplicationv1.databinding.ActivityAddBookBinding
import com.example.bookapplicationv1.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.activity_register.*

class AddBookActivity : AppCompatActivity() {
    // binding
    private lateinit var binding: ActivityAddBookBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arraylist to hold categorias
    private lateinit var spinnerTextSize: Spinner
    private val contentArray by lazy { resources.getStringArray(R.array.Categoria) }
    private var categoryBook = ""

    // pdf
    private var pdfUri: Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    //ticked
    private var isTicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dropdown spinner
        spinnerTextSize = findViewById(R.id.bookCategory)
        val adapter =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, contentArray)
        spinnerTextSize.adapter = adapter

        //checkbox
        bookCryptedPassword.visibility = View.INVISIBLE

        binding.bookCryptedBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isTicked = isChecked
                bookCryptedPassword.visibility = View.VISIBLE
                bookCryptedPasswordText.visibility = View.VISIBLE
            } else {
                isTicked = isChecked
                bookCryptedPasswordText.setText("")
                bookCryptedPassword.visibility = View.INVISIBLE

            }
        }

        // Select Category
        spinnerTextSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                categoryBook = contentArray[p2].toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // back button
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        //instance of firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //setup progressdialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        // Attach PDF
        binding.attachPDF.setOnClickListener {
            pdfPickIntent()
        }

        binding.submitBook.setOnClickListener {
            validateData()
        }
    }

    private var titulo = ""
    private var descricao = ""
    private var categoria = ""
    private var livroEncriptacao = ""
    private var autor = ""

    private fun validateData() {
        titulo = binding.bookTitle.text.toString().trim()
        descricao = binding.bookDescription.text.toString().trim()
        categoria = categoryBook.trim()
        livroEncriptacao = binding.bookCryptedPasswordText.text.toString()
        autor = binding.autorText.text.toString()

        // validar data
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Insira o titulo do livro", Toast.LENGTH_SHORT).show()
        } else if (descricao.isEmpty()) {
            Toast.makeText(this, "Insira a descrição do livro", Toast.LENGTH_SHORT).show()
        } else if (categoria.isEmpty()) {
            Toast.makeText(this, "Insira a categoria do livro", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Insira o PDF do livro", Toast.LENGTH_SHORT).show()
        } else if (autor.isEmpty()){
            Toast.makeText(this, "Insira o autor do livro", Toast.LENGTH_SHORT).show()
        }else{
            uploadPDFToStorage()
        }
    }

    private fun uploadPDFToStorage() {
        progressDialog.setMessage("Inserindo o PDF...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        // caminho do pdf no storage
        val filePathAndName = "Livros/$timestamp"
        // referencia do storage
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this, "PDF inserido, capturando o url...", Toast.LENGTH_SHORT).show()

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfURL = "${uriTask.result}"

                uploadPDFInfoTodb(uploadedPdfURL, timestamp)
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload pdf due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()

                progressDialog.dismiss()
            }

    }

    private fun uploadPDFInfoTodb(uploadedPdfURL: String, timestamp: Long) {
        progressDialog.setMessage("Inserindo a informação do PDF...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["titulo"] = "$titulo"
        hashMap["id"] = "$timestamp"
        hashMap["descricao"] = "$descricao"
        hashMap["categoria"] = "$categoria"
        hashMap["livroEncriptacao"] = "$livroEncriptacao"
        hashMap["url"] = "$uploadedPdfURL"
        hashMap["timestamp"] = timestamp
        hashMap["totalreviews"] = 0
        hashMap["atualreviews"] = 0
        hashMap["autor"] = "$autor"


        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Livro inserido com sucesso ! ",
                    Toast.LENGTH_SHORT
                ).show()
                pdfUri = null
                bookTitle.setText("")
                bookDescription.setText("")
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Erro na atualização de base dados devido ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent ")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)

    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    "PDF selecionado",
                    Toast.LENGTH_SHORT
                ).show()
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick cancelled ")
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

}