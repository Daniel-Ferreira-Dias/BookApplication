package com.example.bookapplicationv1.dialogs


import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.bookapplicationv1.EditProfileActivity
import com.example.bookapplicationv1.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_dialog_changepassword.*

class DialogChangePasswordFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser


    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_changepassword, container, false)


        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser!!

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Por favor espere...")
        progressDialog.setCanceledOnTouchOutside(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changePassword.setOnClickListener {

            var dialog = CustomDialogFragment()
            dialog.show(childFragmentManager, "customDialog")


        }
        editProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        verifyuserEmail.setOnClickListener {
            if (firebaseUser.isEmailVerified) {
                Toast.makeText(requireContext(), "Já verificado", Toast.LENGTH_SHORT).show()

            } else {
                emailVerificationDialog()
            }
        }
        changeEmail.setOnClickListener {
            var dialog = EditEmailFragment()
            dialog.show(childFragmentManager, "editEmail")
        }
    }

    private fun emailVerificationDialog() {
        var builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Verifica o email")
            .setMessage("Deseja verificar o seu email ${firebaseUser.email} ?")
            .setPositiveButton("Enviar") { d, e ->
                sendEmailVerification()
            }
            .setNegativeButton("Cancelar") { d, e ->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        //progress dialog
        progressDialog.setMessage("Enviando a verificação para ${firebaseUser.email}")
        progressDialog.show()

        // instruçoes
        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Email enviado, verifique o seu email ${firebaseUser.email} !", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Falha no enviado devido ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}