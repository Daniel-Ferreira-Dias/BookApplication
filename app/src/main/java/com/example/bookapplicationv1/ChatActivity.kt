package com.example.bookapplicationv1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.classes.Message
import com.example.bookapplicationv1.dialogs.CustomCommentFragment
import com.example.bookapplicationv1.dialogs.CustomTradeFragment
import com.example.bookapplicationv1.fragments.adapters.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    var recieverRoom: String? = null
    var senderRoom: String? = null

    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        val name = intent.getStringExtra("fullName")
        val recieverUID = intent.getStringExtra("uid")

        currentUsername.text = name

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = recieverUID + senderUid
        recieverRoom = senderUid + recieverUID

        supportActionBar?.title = name

        chatRecyclerView = findViewById(R.id.chatRycycleView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendMessage)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        backArrow.setOnClickListener {
            onBackPressed()
        }

        //logic for adding data to recyclerview
        mDbRef.child("Chats").child(senderRoom!!).child("Messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {

                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)

                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        userNamePic.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("recieverUID", recieverUID) // load respetive user profile
            startActivity(intent)
        }

        currentUsername.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("recieverUID", recieverUID) // load respetive user profile
            startActivity(intent)
        }

        // adding message to database
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = Message(message, senderUid)

            mDbRef.child("Chats").child(senderRoom!!).child("Messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("Chats").child(recieverRoom!!).child("Messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")

        }

        //trade handle
        trade.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("recieverUID", recieverUID)
            var dialog = CustomTradeFragment()
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "tradeDialog")
        }


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(recieverUID.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val profileImage = "${snapshot.child("userProfilePic").value}"

                    //set image
                    try {
                        Glide.with(this@ChatActivity).load(profileImage)
                            .placeholder(R.drawable.usericon).into(userNamePic)
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}