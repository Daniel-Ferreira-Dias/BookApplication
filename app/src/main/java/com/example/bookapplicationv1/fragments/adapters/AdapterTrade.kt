package com.example.bookapplicationv1.fragments.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.R
import com.example.bookapplicationv1.classes.ModelPDF
import com.example.bookapplicationv1.classes.Trade
import com.example.bookapplicationv1.databinding.RowBooksBinding
import com.example.bookapplicationv1.databinding.RowTradeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class AdapterTrade : RecyclerView.Adapter<AdapterTrade.HolderTrader>{

    // context, get using construtor
    private var context: Context

    //arraylist to hold pdfs
    private var tradeArrayList: ArrayList<Trade>

    //viewbinding row_pdf_user.xml
    private lateinit var binding: RowTradeBinding

    //firebase
    private val mAuth = FirebaseAuth.getInstance()

    //codes
    var ownerCode = ""
    var traderCode = ""

    constructor(context: Context, tradeArrayList: ArrayList<Trade>) {
        this.context = context
        this.tradeArrayList = tradeArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderTrader {
        binding = RowTradeBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderTrader(binding.root)
    }

    override fun onBindViewHolder(holder: HolderTrader, position: Int) {
        val model = tradeArrayList[position]
        val traderUID = model.tradeOwner
        val status = model.status



        // load user pics
        loadOwnerprofilePic(model, holder)
        loadTraderprofilePic(model, holder)

        //load book details
        loadOwnerBook(model, holder)
        loadTargetBook(model, holder)

        // status
        if (status == 1){
            holder.status.text = "À espera da resposta"
            holder.deleteTrade.visibility = View.INVISIBLE
            holder.status.setTextColor(Color.parseColor("#000000"))
        }else if (status == 2){
            holder.status.text = "Rejeitado"
            holder.status.setTextColor(Color.parseColor("#ad0a0a"))
            holder.rejectBtn.visibility = View.INVISIBLE
            holder.acceptBtn.visibility = View.INVISIBLE
            holder.deleteTrade.visibility = View.VISIBLE
        }else if (status == 3){
            holder.status.text = "Aceite"
            holder.status.setTextColor(Color.parseColor("#0aad3f"))
            holder.rejectBtn.visibility = View.INVISIBLE
            holder.acceptBtn.visibility = View.INVISIBLE
            holder.deleteTrade.visibility = View.VISIBLE
        }

        if (mAuth.uid == traderUID){
            holder.rejectBtn.visibility = View.INVISIBLE
            holder.acceptBtn.visibility = View.INVISIBLE
        }else{
            holder.acceptBtn.setOnClickListener {
                updateAcceptedStatus(model, holder)
            }
            holder.rejectBtn.setOnClickListener {
                updatedRejectedStatus(model, holder)
            }
        }

        holder.deleteTrade.setOnClickListener {
            deleteTrade(model, holder)
        }

    }



    override fun getItemCount(): Int {
        return tradeArrayList.size
    }

    inner class HolderTrader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val status = binding.status
        val currentUserProfilePic = binding.currentUserProfilePic
        val otherUserProfilePic = binding.otherUserProfilePic
        val currentUserPDF = binding.currentUserPDF
        val otherUserPdf = binding.otherUserPdf
        val bookCode = binding.bookCode
        val otherbookCode = binding.otherbookCode
        val rejectBtn = binding.rejectBtn
        val acceptBtn = binding.acceptBtn
        val deleteTrade = binding.deleteTrade
    }

    // ---- FUNCTIONS ---- //

    // Load user pictures

    private fun loadOwnerprofilePic(model: Trade, holder: HolderTrader) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(model.tradeOwner)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profilePic = "${snapshot.child("userProfilePic").value}"

                    // set image
                    try {
                        Glide.with(context)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_user_default)
                            .into(holder.currentUserProfilePic)
                    } catch (e: Exception) {
                        holder.currentUserProfilePic.setImageResource(R.drawable.ic_user_default)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadTraderprofilePic(model: Trade, holder: AdapterTrade.HolderTrader) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(model.tradeReciever)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profilePic = "${snapshot.child("userProfilePic").value}"

                    // set image
                    try {
                        Glide.with(context)
                            .load(profilePic)
                            .placeholder(R.drawable.ic_user_default)
                            .into(holder.otherUserProfilePic)
                    } catch (e: Exception) {
                        holder.otherUserProfilePic.setImageResource(R.drawable.ic_user_default)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    // Load book Titles

    private fun loadOwnerBook(model: Trade, holder: HolderTrader) {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(model.currentUserselectedBookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val title = "${snapshot.child("titulo").value}"
                    ownerCode = "${snapshot.child("livroEncriptacao").value}"

                    if (model.status == 3){
                        holder.bookCode.text = ownerCode
                    }

                    holder.currentUserPDF.text = title

                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadTargetBook(model: Trade, holder: HolderTrader) {
        val ref = FirebaseDatabase.getInstance().getReference("Livros")
        ref.child(model.otherUserselectedBookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val title = "${snapshot.child("titulo").value}"
                    traderCode = "${snapshot.child("livroEncriptacao").value}"
                    holder.otherUserPdf.text = title

                    if (model.status == 3){
                        holder.otherbookCode.text = traderCode
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    // Update Status

    private fun updateAcceptedStatus(model: Trade, holder: HolderTrader) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(model.tradeOwner).child("Trade").child(model.id)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var estado = "${snapshot.child("status").value}".toInt()

                    estado += 2

                    ref.child(model.tradeOwner).child("Trade").child(model.id).child("status").setValue(estado)

                    holder.rejectBtn.visibility = View.INVISIBLE
                    holder.acceptBtn.visibility = View.VISIBLE
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        val mref = FirebaseDatabase.getInstance().getReference("Users")
        mref.child(model.tradeReciever).child("Trade").child(model.id)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var estado = "${snapshot.child("status").value}".toInt()

                    estado += 2

                    ref.child(model.tradeReciever).child("Trade").child(model.id).child("status").setValue(estado)
                    Toast.makeText(context, "Trade aceite!", Toast.LENGTH_SHORT).show()

                    holder.rejectBtn.visibility = View.INVISIBLE
                    holder.acceptBtn.visibility = View.VISIBLE
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun updatedRejectedStatus(model: Trade, holder: HolderTrader) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(model.tradeOwner).child("Trade").child(model.id)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var estado = "${snapshot.child("status").value}".toInt()

                    estado += 1

                    ref.child(model.tradeOwner).child("Trade").child(model.id).child("status").setValue(estado)

                    holder.rejectBtn.visibility = View.INVISIBLE
                    holder.acceptBtn.visibility = View.INVISIBLE

                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        val mref = FirebaseDatabase.getInstance().getReference("Users")
        mref.child(model.tradeReciever).child("Trade").child(model.id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Trade rejeitada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ocorreu um erro na rejeição da trade", Toast.LENGTH_SHORT).show()
            }

    }

    private fun deleteTrade(model: Trade, holder: AdapterTrade.HolderTrader) {
        val mref = FirebaseDatabase.getInstance().getReference("Users")
        mref.child(mAuth.uid!!).child("Trade").child(model.id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Trade apagada com sucesso", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Não foi possivel apagar a trade devido a um erro.", Toast.LENGTH_SHORT).show()
            }
    }
}