package com.example.bookapplicationv1.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.classes.ModelComment
import com.example.bookapplicationv1.fragments.adapters.ReviewListAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment {

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewList: ArrayList<ModelComment>
    private lateinit var reviewAdapter: ReviewListAdapter


    constructor()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        reviewList = ArrayList()
        reviewAdapter = ReviewListAdapter(requireContext(), reviewList)
        reviewRecyclerView = view.findViewById(R.id.reviewbook)
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewRecyclerView.adapter = reviewAdapter

        loadAllReviews()


        return view
    }

    private fun loadAllReviews() {
        val ref = FirebaseDatabase.getInstance().getReference("Comentários")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelComment::class.java)
                    reviewList.add(model!!)
                    reviewList.sortByDescending {
                        it.timestamp
                    }
                    reviewbook.adapter = reviewAdapter
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //loadAllReviews()
    }


}