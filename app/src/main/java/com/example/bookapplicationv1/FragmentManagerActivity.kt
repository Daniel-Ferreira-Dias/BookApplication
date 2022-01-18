package com.example.bookapplicationv1


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.bookapplicationv1.databinding.ActivityFragmentManagerBinding
import com.example.bookapplicationv1.fragments.HomeFragment
import com.example.bookapplicationv1.fragments.MessageFragment
import com.example.bookapplicationv1.fragments.ProfileFragment
import com.example.bookapplicationv1.fragments.SearchFragment
import com.example.bookapplicationv1.fragments.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_fragment_manager.*
import java.text.FieldPosition

class FragmentManagerActivity : AppCompatActivity() {


    // view binding
    private lateinit var binding : ActivityFragmentManagerBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // database
    private lateinit var databaseReference: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()



        setupTabs()

        logoutText.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(this, "Terminou a sessão", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@FragmentManagerActivity, LoginActivity::class.java))
            finish()
        }

        addBookPdf.setOnClickListener {
            startActivity(Intent(this@FragmentManagerActivity, AddBookActivity::class.java))
        }
    }

    private fun setupTabs() {
        val adapter = ViewPagerAdapter(this, tabs.tabCount)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 4
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback()
        {
            override fun onPageSelected(position: Int){
                tabs.selectTab(tabs.getTabAt(position))
            }
        })

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

    }


    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            // not logged in go to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        else{
            readData(firebaseUser)
        }
    }

    private fun readData(firebaseUser: FirebaseUser?) {
        val uid = firebaseUser?.uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = "${snapshot.child("fullName").value}"

                    //data
                    binding.userUserName.text = userName
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })


    }
}