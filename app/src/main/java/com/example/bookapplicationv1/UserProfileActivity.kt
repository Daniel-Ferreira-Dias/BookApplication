package com.example.bookapplicationv1

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.classes.ModelPDF
import com.example.bookapplicationv1.databinding.ActivityUserProfileBinding
import com.example.bookapplicationv1.fragments.adapters.AdaptarCollection
import com.example.bookapplicationv1.fragments.adapters.adapterFavorite
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.accountType
import kotlinx.android.synthetic.main.fragment_profile.emailText
import kotlinx.android.synthetic.main.fragment_profile.profileCollection
import kotlinx.android.synthetic.main.fragment_profile.profilePic
import kotlinx.android.synthetic.main.fragment_profile.userProfileName
import kotlinx.android.synthetic.main.fragment_profile.userTimestamp
import java.lang.Exception
import java.util.ArrayList


class UserProfileActivity : AppCompatActivity() {

    private var userID = ""
    private var userUID = ""

    //view binding
    private lateinit var binding : ActivityUserProfileBinding

    //pdf
    //pdfviewer
    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var collectionArrayList: ArrayList<ModelPDF>
    private lateinit var adaptarCollection: AdaptarCollection

    //favorite viewer
    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteArrayList: ArrayList<ModelPDF>
    private lateinit var favoriteCollection: adapterFavorite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id from intent
        userID = intent.getStringExtra("recieverUID")!!

        // loaduserDetails
        loadUserInfo()

        //back button
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // return home button
        binding.backHome.setOnClickListener {
            val intent = Intent(this, FragmentManagerActivity::class.java)
            startActivity(intent)
            finish()
        }

        //load books
        //pdv viewer
        collectionArrayList = ArrayList()
        adaptarCollection = AdaptarCollection(this, collectionArrayList)
        bookRecyclerView = findViewById(R.id.profileCollection)
        bookRecyclerView.layoutManager = GridLayoutManager(this, 2)
        bookRecyclerView.adapter = adaptarCollection
        loadAllBooks()

        //load favorites
        //pdv viewer
        favoriteArrayList = ArrayList()
        favoriteCollection = adapterFavorite(this, favoriteArrayList)
        favoriteRecyclerView = findViewById(R.id.favoriteCollection)
        favoriteRecyclerView.layoutManager = GridLayoutManager(this, 2)
        favoriteRecyclerView.adapter = favoriteCollection
        loadFavoriteBooks()

        // load first recylcer
        favoriteRecyclerView.visibility = View.INVISIBLE

        favoritesText.setTypeface(null,Typeface.NORMAL)
        favoritesText.setOnClickListener {
            favoriteRecyclerView.visibility = View.VISIBLE
            bookRecyclerView.visibility = View.INVISIBLE
            favoritesText.setTypeface(null,Typeface.BOLD)
            userNameCollectionTextView.setTypeface(null,Typeface.NORMAL)
            myCollectionTextViewTest.setTypeface(null,Typeface.NORMAL)
        }
        userNameCollectionTextView.setOnClickListener {
            favoriteRecyclerView.visibility = View.INVISIBLE
            bookRecyclerView.visibility = View.VISIBLE
            favoritesText.setTypeface(null,Typeface.NORMAL)
            userNameCollectionTextView.setTypeface(null,Typeface.BOLD)
            myCollectionTextViewTest.setTypeface(null,Typeface.BOLD)
        }
    }

    // Load user info
    private fun loadUserInfo() {
        // check if user is verified
        val mref = FirebaseDatabase.getInstance().getReference("Users")
        mref.child(userID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("userEmail").value}"
                    val name = "${snapshot.child("fullName").value}"
                    val profileImage = "${snapshot.child("userProfilePic").value}"
                    val timestamp = "${snapshot.child("userTimestamp").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val uid = "${snapshot.child("userUID").value}"

                    //format data
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    userUID = uid

                    //convert timestamp to proper date
                    //set data
                    userTimestamp.text = date
                    accountType.text = userType
                    emailText.text = email
                    userProfileName.text = name
                    userNameCollectionTextView.text = name

                    //set image
                    try {
                        Glide.with(this@UserProfileActivity).load(profileImage)
                            .placeholder(R.drawable.ic_user_default).into(profilePic)
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAllBooks() {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                collectionArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPDF::class.java)
                    if (model!!.uid == userUID) {
                        collectionArrayList.add(model!!)
                    }

                }
                //setup adapter
                profileCollection.adapter = adaptarCollection
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadFavoriteBooks() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(userID).child("Favoritos")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoriteArrayList.clear()
                    for (ds in snapshot.children){
                        val bookId = "${ds.child("bookId").value}"

                        val modelPDF = ModelPDF()
                        modelPDF.id = bookId

                        favoriteArrayList.add(modelPDF)
                    }
                    favoriteRecyclerView.adapter = favoriteCollection
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}