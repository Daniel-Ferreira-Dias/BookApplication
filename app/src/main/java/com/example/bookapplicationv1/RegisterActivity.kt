package com.example.bookapplicationv1

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.bookapplicationv1.classes.User
import com.example.bookapplicationv1.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // view binding
    private lateinit var binding:ActivityRegisterBinding

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    // database
    private lateinit var database : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        //checkbox
        var isTicked = false

        checkbox.setOnCheckedChangeListener { buttonview, isChecked ->
            isTicked = isChecked
        }

        // terms
        termAndConditions()

        binding.doLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

        binding.RegisterButton.setOnClickListener {
            if (isTicked){
                validateData()
            }
            else{
                Toast.makeText(this, "Tem que concordar com os termos e condição", Toast.LENGTH_SHORT).show()
            }
        }



        Handler().postDelayed({
            tv_text.isSelected = true
        }, 3000)
    }

    private fun termAndConditions(){
        val spannableString = SpannableString("Declaro que li e aceito os termos e condições")

        val clickableSpan = object : ClickableSpan(){
            override fun onClick(widget: View) {
                Toast.makeText(this@RegisterActivity, "Incompleto...", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(clickableSpan, 17, 45, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        conditionsView.text = spannableString
        conditionsView.movementMethod = LinkMovementMethod.getInstance()
    }

    private var fullName = ""
    private var email = ""
    private var password = ""
    private var userName = ""


    private fun validateData(){
        // 1 - Data Input
        val cPassword = binding.passwordconfirmText.text.toString().trim()
        fullName = binding.nomeUtilizador.text.toString().trim()
        email = binding.email.text.toString().trim()
        password = binding.passwordText.text.toString().trim()

        // 2 - Validate Data

        if (fullName.isEmpty()){
            Toast.makeText(this, "Insira o seu nome...", Toast.LENGTH_SHORT).show()
        }else if (email.isEmpty()){
            Toast.makeText(this, "Insira um email ...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Insira um email valido", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Insira o seu password...", Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()){
            Toast.makeText(this, "Confirma a password...", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword){
            Toast.makeText(this, "A password não é igual...", Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //3 - Criar Conta

        // show progress
        progressDialog.setMessage("Criando a conta...")
        progressDialog.show()

        // criar conta na firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // conta criada, adicionar info na database
                updateUserInfo()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Registo sem successo devido ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        //4 - Guardar informação do utilizador
        progressDialog.setMessage("Guardando a informação do utilizador...")

        // Timestamp
        val timestamp = System.currentTimeMillis()

        // get current user uid
        val uid = firebaseAuth.uid.toString()

        //userType default
        val userType = "User" // Can be admin or user, if admin, will change on database manually

        //userDesc
        val userVerified = false

        //val profilepic
        val userProfile = ""

        database = FirebaseDatabase.getInstance().getReference("Users")
        val User = User(fullName, email, userType, timestamp, userVerified, uid, userProfile)
        database.child(uid).setValue(User).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Conta criada com sucesso...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@RegisterActivity, FragmentManagerActivity::class.java))
            finish()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Conta criada sem sucesso...", Toast.LENGTH_SHORT).show()
        }

    }

}