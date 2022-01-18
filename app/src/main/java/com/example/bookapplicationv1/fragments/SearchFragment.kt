package com.example.bookapplicationv1.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.fragments.adapters.AdapaterPDF
import com.example.bookapplicationv1.classes.ModelPDF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class SearchFragment : Fragment {

    private lateinit var bookRecyclerView: RecyclerView
    private lateinit var pdfArrayList: ArrayList<ModelPDF>
    private lateinit var adapaterPDF : AdapaterPDF


    private val mAuth = FirebaseAuth.getInstance()
    private val user:FirebaseUser? = mAuth.currentUser
    private val uid = user?.uid.toString()

    lateinit var searchSettings: ImageButton


    constructor()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchSettings = view.findViewById(R.id.searchSettings)
        searchSettings.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), searchSettings)
            popupMenu.menu.add(Menu.NONE, 0, 0, "Autor")
            popupMenu.menu.add(Menu.NONE, 1, 1, "Categoria")
            popupMenu.menu.add(Menu.NONE, 2, 2, "Nome do livro")
            popupMenu.show()
        }


        pdfArrayList = ArrayList()
        adapaterPDF = AdapaterPDF(requireContext(), pdfArrayList)
        bookRecyclerView = view.findViewById(R.id.booksList)
        bookRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        bookRecyclerView.adapter=adapaterPDF
        bookRecyclerView.scrollState


        loadAllBooks()




        return view
    }

    override fun onResume() {
        super.onResume()
        //loadAllBooks()
    }




    private fun loadAllBooks() {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPDF::class.java)
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                booksList.adapter = adapaterPDF
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }




}


