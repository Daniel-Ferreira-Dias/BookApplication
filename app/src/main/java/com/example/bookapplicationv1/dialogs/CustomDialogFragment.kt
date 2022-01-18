package com.example.bookapplicationv1.dialogs

import android.content.Intent
import android.os.Bundle
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
import kotlinx.android.synthetic.main.modal_diagram_layout.currentPasswordText
import kotlinx.android.synthetic.main.modal_diagram_layout.newPasswordConfirmText
import kotlinx.android.synthetic.main.modal_diagram_layout.newPasswordText
import kotlinx.android.synthetic.main.modal_diagram_layout.view.*

class CustomDialogFragment : DialogFragment() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomBottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_diagram_layout, container, false)

        auth = FirebaseAuth.getInstance()

        view.backBtn.setOnClickListener {
            dismiss()
        }

        view.ConfirmBtn.setOnClickListener {
            changePassword()
        }

        return view
    }


    private fun changePassword(){
        if (currentPasswordText.text!!.isNotEmpty() && newPasswordText.text!!.isNotEmpty() && newPasswordConfirmText.text!!.isNotEmpty()){
            if(newPasswordText.text.toString() == newPasswordConfirmText.text.toString()) {
                if (newPasswordText.text!!.length >= 6) {
                    reAuthenticate()
                } else{
                    Toast.makeText(requireContext(),"A password tem que ter mais do 5 letras." , Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext(),"As password novas não são iguais..." , Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(requireContext(),"Preencha todos os campos..." , Toast.LENGTH_SHORT).show()
        }
    }

    private fun reAuthenticate() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null && user.email != null){
            val credential = EmailAuthProvider
                .getCredential(user.email!!, currentPasswordText.text.toString() )
            user.reauthenticate(credential)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        changePasswordAuth()

                    }else{
                        Toast.makeText(requireContext(),"Ocorreu um erro na re-autenticação..." ,Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun changePasswordAuth() {
        val user: FirebaseUser? = auth.currentUser
        user!!.updatePassword(newPasswordText.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(),"Palavra-passe alterada com sucesso" ,Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                }
            }
    }
}