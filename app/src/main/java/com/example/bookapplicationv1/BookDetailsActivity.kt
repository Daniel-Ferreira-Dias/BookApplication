package com.example.bookapplicationv1

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.databinding.ActivityBookDetailsBinding
import com.example.bookapplicationv1.databinding.DialogCommentAddBinding
import com.example.bookapplicationv1.databinding.FragmentEncryptedDialogBinding
import com.example.bookapplicationv1.fragments.adapters.AdapterComment
import com.example.bookapplicationv1.classes.Constants
import com.example.bookapplicationv1.classes.ModelComment
import com.example.bookapplicationv1.dialogs.CustomCommentFragment
import com.example.bookapplicationv1.dialogs.DialogChangePasswordFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_book_details.*
import kotlinx.android.synthetic.main.activity_book_details.view.*
import kotlinx.android.synthetic.main.activity_edit_book.*
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.random.Random


class BookDetailsActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityBookDetailsBinding

    //book id
    public var bookId = ""

    //private val
    private var encrypted = ""

    //book details
    private var bookTitle = ""
    private var bookUrl = ""

    //comment
    private var comment = ""
    private var bookRating = ""

    //useruid
    private var userUID = ""

    //rating
    private var totalRating = 0
    private var currentRating = 0F
    private var finalrating = 0F

    //favoritos
    private var isInMyFavorite = false

    // dialog
    private lateinit var progressDialog: ProgressDialog

    // firebase auth
    private val mAuth = FirebaseAuth.getInstance()

    //comment arraylist
    private lateinit var commentArrayList: ArrayList<ModelComment>
    //adaptar for recycler
    private lateinit var adapterComment: AdapterComment
    //recycler
    private lateinit var commentRecyclerView: RecyclerView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)
        //load book
        loadBookDetails()
        //check if book is in favorite
        if (mAuth.currentUser != null){
            checkIfFavorite()
        }
        // comments
        showComments()
        //arraylist
        commentArrayList = ArrayList()
        adapterComment = AdapterComment(this, commentArrayList)
        commentRecyclerView = findViewById(R.id.commentsRV)
        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        commentRecyclerView.adapter = adapterComment

        //handle back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // to be able to read, write correct password of the book
        binding.readBookBtn.setOnClickListener {
            if (encrypted == "") {
                val intent = Intent(this, PdfViewActivity::class.java)
                intent.putExtra("bookId", bookId)
                startActivity(intent)
            } else {
                addEncryptedDialog()
            }
        }

        // to be able to download, write correct password of the book
        binding.downloadBookBtn.setOnClickListener {
            if (encrypted == "") {
                checkFirstStageDownloadPermission()
            } else {
                addEncryptedDialogDownload()
            }
        }

        // go to his profile
        binding.publisherId.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("recieverUID", userUID )
            startActivity(intent)
        }

        // to be able to add a comment
        binding.commentBtn.setOnClickListener {
            if (mAuth.currentUser == null){
                Toast.makeText(this, "Tem que fazer o login para efetuar um comentário", Toast.LENGTH_SHORT).show()
            }else{
                val bundle = Bundle()
                bundle.putString("bookId", bookId)
                val dialog = CustomCommentFragment()
                dialog.arguments = bundle
                dialog.show(supportFragmentManager, "Comment")
            }
        }

        //handle favorites
        binding.favoriteImage.setOnClickListener {
            if (mAuth.currentUser == null){
                Toast.makeText(this, "Tem que fazer o login para adicionar este livro aos favoritos", Toast.LENGTH_SHORT).show()
            }else{
                if (isInMyFavorite){
                    removeFromFavorite()
                }else{
                    addToFavorite()
                }
            }
        }

    }


    // ---- Book Download Functions ---- //

    private fun checkFirstStageDownloadPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            downloadBook()
        } else {
            requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                downloadBook()
            } else {
                Toast.makeText(this, "Permissão rejeitada", Toast.LENGTH_SHORT).show()
            }
        }

    private fun downloadBook() {
        //progress bar
        progressDialog.setMessage("Realizando o download do livro...")
        progressDialog.show()
        //
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveToDownloadFolder(bytes)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Download sem sucesso devido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {
        val nameWithExtension = "$bookTitle.pdf"

        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()

            val filePath = downloadsFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Guardado no ficheiro de Download", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Falha ao guardar devido a ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // ---- Encrypted Dialog to Read ---- //

    private fun addEncryptedDialog() {
        val encryptedAddBinding = FragmentEncryptedDialogBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(encryptedAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click, dismiss dialog
        encryptedAddBinding.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        var checkPassword = ""

        encryptedAddBinding.submitPassword.setOnClickListener {
            checkPassword = encryptedAddBinding.bookPasswordText.text.toString().trim()
            if (checkPassword == encrypted) {
                generatorPassword()
            } else {
                Toast.makeText(this, "Palavra passe incorreta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---- Generate Password ---- //

    private fun generatorPassword() {
        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random(System.nanoTime())
        val password = StringBuilder()

        for (i in 0 until 8) {
            val rIndex = random.nextInt(characterSet.length)
            password.append(characterSet[rIndex])
        }

        val codigo = password.toString().trim()

        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId).child("livroEncriptacao").setValue(codigo)
            .addOnSuccessListener {
                val intent = Intent(this, PdfViewActivity::class.java)
                intent.putExtra("bookId", bookId)
                startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(this, "Não foi possivel obter a leitura do livro...", Toast.LENGTH_SHORT).show()
            }


    }

    private fun generatorPasswordforDownload() {
        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random(System.nanoTime())
        val password = StringBuilder()

        for (i in 0 until 8) {
            val rIndex = random.nextInt(characterSet.length)
            password.append(characterSet[rIndex])
        }

        val codigo = password.toString().trim()

        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId).child("livroEncriptacao").setValue(codigo)
            .addOnSuccessListener {
                checkFirstStageDownloadPermission()
            }.addOnFailureListener {
                Toast.makeText(this, "Não foi possivel obter a leitura do livro...", Toast.LENGTH_SHORT).show()
            }


    }

    // ---- Encrypted Dialog to Download ---- //

    private fun addEncryptedDialogDownload() {
        val encryptedAddBinding = FragmentEncryptedDialogBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(encryptedAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click, dismiss dialog
        encryptedAddBinding.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        var checkPassword = ""

        encryptedAddBinding.submitPassword.setOnClickListener {
            checkPassword = encryptedAddBinding.bookPasswordText.text.toString().trim()
            if (checkPassword == encrypted) {
                generatorPasswordforDownload()
                //checkFirstStageDownloadPermission()
                alertDialog.dismiss()
            } else {
            }
        }
    }

    // ---- Load Book Details to Page ---- //

    private fun loadBookDetails() {
        //Books > BookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get data
                    val categoria = "${snapshot.child("categoria").value}"
                    val descricao = "${snapshot.child("descricao").value}"
                    bookTitle = "${snapshot.child("titulo").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val livroEncriptacao = "${snapshot.child("livroEncriptacao").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val booktotalrating = "${snapshot.child("totalreviews").value}"
                    val bookcurrentraing = "${snapshot.child("atualreviews").value}"
                    val autor = "${snapshot.child("autor").value}"

                    totalRating = booktotalrating.toInt()
                    currentRating = bookcurrentraing.toFloat()

                    //final rating
                    finalrating = currentRating/totalRating

                    encrypted = livroEncriptacao

                    userUID = uid

                    //get user who published
                    val mref = FirebaseDatabase.getInstance().getReference("Users")
                    mref.ref.child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val publishedId = "${snapshot.child("fullName").value}"

                                binding.publisherId.text = publishedId
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })

                    //format data
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    //load pdf
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfViewer,
                        binding.progressBar,
                        null
                    )
                    //load size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeText)

                    // set data
                    binding.titleTv.text = bookTitle
                    binding.bookDescricao.text = descricao
                    binding.categoryTextFinal.text = categoria
                    binding.bookTimeStamp.text = date
                    binding.totalReviewstext.text = totalRating.toString()
                    binding.userRating.rating = finalrating
                    binding.autorName.text = autor


                    if (livroEncriptacao == "") {
                        lockedImage.visibility = View.INVISIBLE
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    // ---- Comment Functions ---- //

    private fun showComments() {
        //db path to load comments
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelComment::class.java)
                        commentArrayList.add(model!!)
                    }
                    commentsRV.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    // ---- Add to favorites ---- //

    private fun addToFavorite(){
        val timestamp = System.currentTimeMillis()

        // set up data
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = mAuth.uid.toString()

        // save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favoritos").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "Não foi possivel adicionar aos favoritos devido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun removeFromFavorite(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favoritos").child(bookId)
            .removeValue().addOnSuccessListener {
                Toast.makeText(this, "Removido dos favoritos!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e->
                Toast.makeText(this, "Não foi possivel remover dos favoritos devida a a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfFavorite(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favoritos").child(bookId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite){
                        binding.favoriteImage.setBackgroundResource(R.drawable.ic_favorite_fill)
                        addFavesText.setText("Remover dos favoritos")
                    }else{
                        binding.favoriteImage.setBackgroundResource(R.drawable.ic_favorite_empty)
                        addFavesText.setText("Adicionar aos Favoritos")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}