package com.example.bookapplicationv1.fragments

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.dialogs.DialogChangePasswordFragment
import com.example.bookapplicationv1.classes.ModelPDF
import com.example.bookapplicationv1.MyApplication
import com.example.bookapplicationv1.fragments.adapters.AdaptarCollection
import com.example.bookapplicationv1.fragments.adapters.adapterFavorite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception
import java.util.*


class ProfileFragment : Fragment() {

    // firebase auth
    private val mAuth = FirebaseAuth.getInstance()
    private val user: FirebaseUser? = mAuth.currentUser

    // Users information
    private val uid = user?.uid.toString()

    //global
    lateinit var profileFragment: TextView
    lateinit var emailText: TextView
    private lateinit var userProfileName: TextView
    lateinit var userTimestamp: TextView
    lateinit var editSettings: TextView
    lateinit var myCollectionTextView : TextView
    lateinit var myFavoriteTextView : TextView

    //pdfviewer
    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var collectionArrayList: ArrayList<ModelPDF>
    private lateinit var adaptarCollection: AdaptarCollection

    //favorites
    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteArrayList: ArrayList<ModelPDF>
    private lateinit var adapaterFavorite: adapterFavorite

    // database
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        profileFragment = view.findViewById(R.id.userProfileName)
        emailText = view.findViewById(R.id.emailText)
        userProfileName = view.findViewById(R.id.userProfileName)
        myCollectionTextView = view.findViewById(R.id.myCollectionTextView)
        myFavoriteTextView = view.findViewById(R.id.myFavoriteTextView)
        userTimestamp = view.findViewById(R.id.userTimestamp)
        editSettings = view.findViewById(R.id.editSettings)

        //pdv viewer
        collectionArrayList = ArrayList()
        adaptarCollection = AdaptarCollection(requireContext(), collectionArrayList)
        bookRecyclerView = view.findViewById(R.id.profileCollection)
        bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        bookRecyclerView.adapter = adaptarCollection

        //favorite viewer
        favoriteArrayList = ArrayList()
        adapaterFavorite = adapterFavorite(requireContext(), favoriteArrayList)
        favoriteRecyclerView = view.findViewById(R.id.favoriteCollection)
        favoriteRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        favoriteRecyclerView.adapter = adapaterFavorite


        // load data
        loadUserInfo()

        //load books
        loadAllBooks()

        //load favorites
        loadFavoriteBooks()

        //INVISIBLE RECYCLER
        favoriteRecyclerView.visibility = View.INVISIBLE
        myFavoriteTextView.setTypeface(null, Typeface.NORMAL)


        editSettings.setOnClickListener {
            var dialog = DialogChangePasswordFragment()
            dialog.show(childFragmentManager, "customDialog")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myFavoriteTextView.setOnClickListener {
            bookRecyclerView.visibility = View.INVISIBLE
            favoriteRecyclerView.visibility = View.VISIBLE
            myCollectionTextView.setTypeface(null, Typeface.NORMAL)
            myFavoriteTextView.setTypeface(null,Typeface.BOLD)
        }

        myCollectionTextView.setOnClickListener {
            bookRecyclerView.visibility = View.VISIBLE
            favoriteRecyclerView.visibility = View.INVISIBLE
            myCollectionTextView.setTypeface(null, Typeface.BOLD)
            myFavoriteTextView.setTypeface(null,Typeface.NORMAL)
        }
    }

    private fun loadFavoriteBooks() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favoritos")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoriteArrayList.clear()
                    for (ds in snapshot.children){
                        val bookId = "${ds.child("bookId").value}"

                        val modelPDF = ModelPDF()
                        modelPDF.id = bookId

                        favoriteArrayList.add(modelPDF)
                    }
                    favoriteRecyclerView.adapter = adapaterFavorite
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onResume() {
        super.onResume()

        /*//pdv viewer
        collectionArrayList = ArrayList()
        adaptarCollection = AdaptarCollection(requireContext(), collectionArrayList)
        bookRecyclerView = requireView().findViewById(R.id.profileCollection)
        bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        bookRecyclerView.adapter = adaptarCollection

        //load books
        loadAllBooks()

        //
        loadFavoriteBooks()*/
    }

    private fun loadAllBooks() {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                collectionArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPDF::class.java)
                    if (model!!.uid == mAuth.uid) {
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

    private fun loadUserInfo() {
        // check if user is verified
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(mAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = user?.email
                    val name = "${snapshot.child("fullName").value}"
                    val profileImage = "${snapshot.child("userProfilePic").value}"
                    val timestamp = "${snapshot.child("userTimestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //format data
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    if (user!!.isEmailVerified) {
                        accountStatus.text = "Verificado"
                    } else {
                        accountStatus.text = "Não verificado"
                    }

                    //convert timestamp to proper date
                    //set data
                    userTimestamp.text = date
                    accountType.text = userType
                    emailText.text = email
                    userProfileName.text = name
                    //set image
                    try {
                        Glide.with(profileFragment).load(profileImage)
                            .placeholder(R.drawable.ic_user_default).into(profilePic)
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}