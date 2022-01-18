package com.example.bookapplicationv1

import android.app.Activity
import android.app.Instrumentation
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.contentValuesOf
import com.bumptech.glide.Glide
import com.example.bookapplicationv1.databinding.ActivityEditProfileBinding
import com.example.bookapplicationv1.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception


class EditProfileActivity : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityEditProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //image uri
    private var imageUri: Uri? = null

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back arrow
        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        // Update Profile info
        binding.saveInfo.setOnClickListener {
            validateData()
        }

        // Update Profile Picture
        binding.profileUpload.setOnClickListener {
            showImageAttachMenu()
        }

        // fire base authentication
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
    }


    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("userEmail").value}"
                    val name = "${snapshot.child("fullName").value}"
                    val profileImage = "${snapshot.child("userProfilePic").value}"
                    //convert timestamp to proper date

                    //set data
                    binding.editUsername.setText(name)


                    //set image
                    try {
                        Glide.with(this@EditProfileActivity).load(profileImage)
                            .placeholder(R.drawable.ic_user_default).into(binding.profileUpload)

                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachMenu() {
        // setup pop menu
        val popupMenu = PopupMenu(this, binding.profileUpload)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        //handle popup menu
        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                // Camera
                pickImageCamera()
            } else if (id == 1) {
                // Gallery
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    private fun pickImageGallery() {
        // pick image from garely
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    // handle for camera
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // set to imageview
                binding.profileUpload.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )


    // handle for gallery
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                // set to imageview
                binding.profileUpload.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private var name = ""

    private fun validateData() {
        name = binding.editUsername.text.toString().trim()

        //validar data
        if (name.isEmpty()) {
            Toast.makeText(this, "Insira o seu nome", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                // upload sem foto
                updateProfile("")
            } else {
                // upload com foto
                uploadImage()
            }
        }
    }

    private fun updateProfile(uploadImageUrl: String) {
        progressDialog.setMessage("Atualizando o perfil...")

        //setup
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["fullName"] = "$name"

        if (imageUri != null) {
            hashMap["userProfilePic"] = uploadImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Perfil atualizado ", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Erro de atualização de perfil devido  ${e.message} ",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Enviando a foto...")
        progressDialog.show()

        val filePathAndName = "ProfileImages" + firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"
                updateProfile(uploadImageUrl)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Erro no envio da imagem devido ${e.message} ",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}