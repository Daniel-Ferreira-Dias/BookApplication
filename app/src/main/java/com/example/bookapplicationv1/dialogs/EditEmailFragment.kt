package com.example.bookapplicationv1.dialogs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.bookapplicationv1.LoginActivity
import com.example.bookapplicationv1.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_editemail_dialog.*
import kotlinx.android.synthetic.main.fragment_editemail_dialog.view.*

class EditEmailFragment : DialogFragment() {



    private lateinit var firebaseUser : FirebaseUser
    private lateinit var auth: FirebaseAuth

    private lateinit var firebaseDatabase: FirebaseDatabase

    private var newEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomBottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editemail_dialog, container, false)



        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser!!

        view.backBtn.setOnClickListener {
            dismiss()
        }

        view.ConfirmBtnChangeEmail.setOnClickListener {
            checkFields()
        }

        return view
    }

    private fun checkFields() {
        if (actualEmail.text.toString() != firebaseUser.email.toString()){
            Toast.makeText(requireContext(), "Esse não é o seu email correto", Toast.LENGTH_SHORT).show()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(editEmail.text).matches()){
            Toast.makeText(requireContext(), "Preenche o campo com um email válido", Toast.LENGTH_SHORT).show()
        }else if (editEmail.text.isEmpty()){
            Toast.makeText(requireContext(), "Preencha o campo com o novo email...", Toast.LENGTH_SHORT).show()
        }else if (confirmPasswordText.text!!.isEmpty()){
            Toast.makeText(requireContext(), "Confirme a palavra passe", Toast.LENGTH_SHORT).show()
        }else{
            newEmail = editEmail.text.toString()
            verificationDialog()
        }
    }


    private fun verificationDialog() {
        var builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Mudar o email")
            .setMessage("Deseja atualizar o email de ${firebaseUser.email} para $newEmail ?")
            .setPositiveButton("Confirmar") { d, e ->
                reAuthenticate()
            }
            .setNegativeButton("Cancelar") { d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun reAuthenticate() {
        if (firebaseUser != null && firebaseUser.email != null){
            val credential = EmailAuthProvider
                .getCredential(actualEmail.text.toString(), confirmPasswordText.text.toString() )
            firebaseUser.reauthenticate(credential)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        changeEmailFunct()
                    }else{
                        Toast.makeText(requireContext(),"Ocorreu um erro na re-autenticação..." ,Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }



    private fun changeEmailFunct() {
        newEmail = editEmail.text.toString()
        firebaseUser.updateEmail(editEmail.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(),"Email atualizado com sucesso..." ,Toast.LENGTH_SHORT).show()
                    auth.signOut()


                    //setup
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["userEmail"] = "$newEmail"


                    //update to db
                    val reference = FirebaseDatabase.getInstance().getReference("Users")
                    reference.child(firebaseUser.uid)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            Toast.makeText(requireContext(), "Perfil atualizado ", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Erro de atualização de perfil devido  ${e.message} ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }
}