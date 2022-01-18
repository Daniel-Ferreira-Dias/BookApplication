package com.example.bookapplicationv1

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapplicationv1.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityForgotPasswordBinding

    //firebase auth
    private lateinit var firebaseAuth : FirebaseAuth

    //progress dialog
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Um momento")
        progressDialog.setCanceledOnTouchOutside(false)

        //begin password reset
        binding.sendEmail.setOnClickListener {
            validateData()
        }

        //go back
        binding.backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private var email = ""

    private fun validateData() {
        // get data
        email = binding.userEmail.text.toString().trim()

        //validar
        if (email.isEmpty()){
            Toast.makeText(this, "Insira o email", Toast.LENGTH_SHORT).show()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
        }else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        progressDialog.setMessage("Enviando email...")
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Email enviado !", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Não foi possivel mandar email!", Toast.LENGTH_SHORT).show()
            }
    }
}