package com.example.bookapplicationv1

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.bookapplicationv1.databinding.ActivityEditBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_edit_book.*
import java.lang.StringBuilder
import kotlin.random.Random


class EditBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBookBinding

    //book id
    private var bookId = ""
    private var codigo = ""

    //arraylist to hold categorias
    private lateinit var spinnerTextSize: Spinner
    private val contentArray by lazy { resources.getStringArray(R.array.Categoria) }
    private var categoryBook = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dropdown spinner
        spinnerTextSize = findViewById(R.id.bookCategory)
        val adapter =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, contentArray)
        spinnerTextSize.adapter = adapter

        // Select Category
        spinnerTextSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                categoryBook = contentArray[p2].toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        //progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Um momento")
        progressDialog.setCanceledOnTouchOutside(false)

        // get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //load book info
        loadBookInfo()

        //random password generator
        binding.randomPassword.setOnClickListener {
            generatorPassword()
        }

        //go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBook.setOnClickListener {
            validateData()
        }

    }

    private fun loadBookInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book info
                    val bookTitle = snapshot.child("titulo").value.toString()
                    val bookDesc = snapshot.child("descricao").value.toString()
                    val bookCateg = snapshot.child("categoria").value.toString()
                    val bookCode = snapshot.child("livroEncriptacao").value.toString()
                    val autor = snapshot.child("autor").value.toString()

                    //set voew
                    binding.autorText.setText(autor)
                    binding.bookTitle.setText(bookTitle)
                    binding.bookDescription.setText(bookDesc)
                    categoryBook = bookCateg

                    if (bookCode == "") {
                        binding.bookCryptedPassword.text = "Não possui código"
                    } else {
                        binding.bookCryptedPassword.text = bookCode
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun generatorPassword() {
        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random(System.nanoTime())
        val password = StringBuilder()

        for (i in 0 until 8) {
            val rIndex = random.nextInt(characterSet.length)
            password.append(characterSet[rIndex])
        }
        Toast.makeText(this, "Código do livro gerado", Toast.LENGTH_SHORT).show()
        codigo = password.toString().trim()
        bookCryptedPassword.text = codigo
    }

    private var titulo = ""
    private var descricao = ""
    private var categoria = ""
    private var autor = ""

    private fun validateData() {
        // get data
        titulo = binding.bookTitle.text.toString().trim()
        descricao = binding.bookDescription.text.toString().trim()
        categoria = categoryBook.trim()
        autor = binding.autorText.text.toString().trim()

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Insira o titulo do livro", Toast.LENGTH_SHORT).show()
        } else if (descricao.isEmpty()) {
            Toast.makeText(this, "Insira a descrição do titulo", Toast.LENGTH_SHORT).show()
        } else if (categoria.isEmpty()) {
            Toast.makeText(this, "Escolha a categoria do livro", Toast.LENGTH_SHORT).show()
        } else if (autor.isEmpty()){
            Toast.makeText(this, "Escolha o autor do livro", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        //show progress
        progressDialog.setMessage("Atualizando o livro")
        progressDialog.show()

        //setupdate to update to db
        val hashMap = HashMap<String, Any>()
        hashMap["titulo"] = "$titulo"
        hashMap["descricao"] = "$descricao"
        hashMap["categoria"] = "$categoria"
        hashMap["livroEncriptacao"] = "$codigo"
        hashMap["autor"] = "$autor"

        //update
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Livro atualizado", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Livro não foi atualizado com sucesso", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}