package com.example.bookapplicationv1.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.classes.Trade
import com.example.bookapplicationv1.classes.User
import com.example.bookapplicationv1.fragments.adapters.AdapaterPDF
import com.example.bookapplicationv1.fragments.adapters.AdapterTrade
import com.example.bookapplicationv1.fragments.adapters.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_message.*


class MessageFragment : Fragment() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter

    private lateinit var traderRecyclerView: RecyclerView
    private lateinit var tradeList: ArrayList<Trade>
    private lateinit var tradeAdapaterTrade: AdapterTrade

    


    private lateinit var mDbRef : DatabaseReference
    private lateinit var mAth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        mDbRef = FirebaseDatabase.getInstance().getReference()
        mAth = FirebaseAuth.getInstance()

        // Trades
        tradeList = ArrayList()
        tradeAdapaterTrade = AdapterTrade(requireContext(), tradeList)
        traderRecyclerView = view.findViewById(R.id.tradeRecycler)
        traderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        traderRecyclerView.adapter = tradeAdapaterTrade
        loadTrades()


        // Users
        userList = ArrayList()
        adapter = UserAdapter(requireContext(), userList)
        userRecyclerView = view.findViewById(R.id.messageFragment)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter
        loadUser()

        traderRecyclerView.visibility = View.INVISIBLE
        return view
    }

    private fun loadTrades() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAth.uid!!).child("Trade")
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                tradeList.clear()
                for (postSnapshot in snapshot.children){
                    val model = postSnapshot.getValue(Trade::class.java)
                    tradeList.add(model!!)
                }
                tradeAdapaterTrade.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadUser() {
        mDbRef.child("Users").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)

                    if (mAth.currentUser?.uid != currentUser?.userUID ){
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchSettings.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), searchSettings)
            popupMenu.menu.add(Menu.NONE, 0, 0, "Mensagens")
            popupMenu.menu.add(Menu.NONE, 1, 1, "Trades")
            popupMenu.show()

            //handle popup menu
            popupMenu.setOnMenuItemClickListener { item ->
                val id = item.itemId
                if (id == 0) {
                    userRecyclerView.visibility = View.VISIBLE
                    traderRecyclerView.visibility = View.INVISIBLE
                } else if (id == 1) {
                    userRecyclerView.visibility = View.INVISIBLE
                    traderRecyclerView.visibility = View.VISIBLE
                }
                true
            }
        }
    }


}