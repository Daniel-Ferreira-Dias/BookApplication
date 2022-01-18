package com.example.bookapplicationv1

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapplicationv1.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()



        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle nao tem conta
        binding.SignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        //handle login
        binding.LoginButton.setOnClickListener {
            validateData()

        }

        //handle forget passoword
        binding.forgetPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        // 1 - input data
        email = binding.etemail.text.toString().trim()
        password = binding.passwordTextLogin.text.toString().trim()

        // 2 - validar data
        if (email.isEmpty()){
            Toast.makeText(this, "Insira o seu email...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Insira um email valido", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Insira o seu password...", Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
        }
    }

    private fun loginUser() {
        // 3) Login


        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // login sucesso
                Toast.makeText(this, "Login efetuado com sucesso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, FragmentManagerActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                // login fail
                progressDialog.dismiss()
                Toast.makeText(this, "Login falhado devido ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}