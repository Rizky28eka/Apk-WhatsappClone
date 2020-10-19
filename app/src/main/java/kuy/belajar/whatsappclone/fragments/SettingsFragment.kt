package kuy.belajar.whatsappclone.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kuy.belajar.whatsappclone.R
import kuy.belajar.whatsappclone.WelcomeActivity
import kuy.belajar.whatsappclone.model.User
import java.io.File

class SettingsFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var dbRef: DatabaseReference
    private lateinit var userListener: ValueEventListener
    private lateinit var userData: User
    private var isCover = false
    private var isUpdateProfile = false
    private var socialLinkType = ""
    private val requestCodeActivity = 438
    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userData = snapshot.getValue(User::class.java) as User
                    with(view) {
                        if (isUpdateProfile) {
                            showToast("Profile has been updated")
                            isUpdateProfile = false
                        }
                        if (userData.profile.isNotBlank()) Picasso.get().load(userData.profile)
                            .placeholder(R.drawable.ic_profile).into(profile_image)
                        if (userData.cover.isNotBlank()) Picasso.get().load(userData.cover)
                            .into(profile_bg)
                        user_name.text = userData.username
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(activity, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        } else {
            firebaseUser = user
            dbRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
            storageRef = FirebaseStorage.getInstance().reference.child("User Images")
            with(view) {
                profile_bg.setOnClickListener {
                    isCover = true
                    pickImage()
                }
                profile_image.setOnClickListener {
                    isCover = false
                    pickImage()
                }
                facebook.setOnClickListener {
                    socialLinkType = "facebook"
                    setSocialLink()
                }
                instagram.setOnClickListener {
                    socialLinkType = "instagram"
                    setSocialLink()
                }
                website.setOnClickListener {
                    socialLinkType = "website"
                    setSocialLink()
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbRef.addValueEventListener(userListener)
    }

    private fun setSocialLink() {
        val alertDialogBuilder =
            AlertDialog.Builder(context, R.style.AlertDialogTheme)

        val editText = EditText(context)

        when (socialLinkType) {
            "website" -> {
                alertDialogBuilder.setTitle("Write Website URL : ")
                editText.hint = "e.g https://www.google.com"
            }
            "instagram" -> {
                alertDialogBuilder.setTitle("Write Instagram URL : ")
                editText.hint = "e.g https://www.instagram.com/imamfahrurrofi/"
            }
            "facebook" -> {
                alertDialogBuilder.setTitle("Write Facebook URL : ")
                editText.hint = "e.g https://www.fb.me/callmefaro"
            }
            else -> {
                socialLinkType = ""
                return
            }
        }
        alertDialogBuilder.run {
            setView(editText)
            setPositiveButton("Update") { dialog, _ ->
                val str = editText.text.toString()
                if (str.isBlank()) showToast("Don't leave blank") else {
                    saveLink(str)
                    dialog.cancel()
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        }
        val alert = alertDialogBuilder.create()
        alert.show()
        val btnPositive = alert.getButton(DialogInterface.BUTTON_POSITIVE)
        btnPositive.setBackgroundColor(Color.GREEN)
        val btnNegative = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        btnNegative.setBackgroundColor(Color.RED)
    }

    private fun saveLink(link: String) {
        when (socialLinkType) {
            "website" -> {
                userData.website = link
            }
            "instagram" -> {
                userData.instagram = link
            }
            "facebook" -> {
                userData.facebook = link
            }
            else -> {
                return
            }
        }
        isUpdateProfile = true
        dbRef.setValue(userData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbRef.removeEventListener(userListener)
    }

    private fun openLink(link: String) {
        val uri = Uri.parse(link)
        val intentWeb = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intentWeb)
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Pick Image"), requestCodeActivity)
    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeActivity && resultCode == Activity.RESULT_OK && data?.data !== null) {
            data.data?.let {
                uploadImageToDatabase(it)
                showToast("Uploading Image..")
            }
        }
    }

    private fun uploadImageToDatabase(image: Uri) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Image is Uploading, please wait.")
        progressDialog.show()

        val fileRef = storageRef.child(System.currentTimeMillis().toString() + ".jpg")
        val uploadTask = fileRef.putFile(image)
        uploadTask.continueWithTask {
            if (!it.isSuccessful) {
                it.exception?.let { error ->
                    throw error
                }
            }
            return@continueWithTask fileRef.downloadUrl
        }.addOnCompleteListener { taskUploadNew ->
            if (taskUploadNew.isSuccessful) {
                val downloadUrl = taskUploadNew.result.toString()
                if (isCover) {
                    if (userData.cover.isNotBlank()) {
                        val coverOld = File(userData.cover)
                        val coverOldName = coverOld.nameWithoutExtension.drop(16)
                        val coverRef = storageRef.child(coverOldName + ".jpg").delete()
                        coverRef.addOnCompleteListener { taskDeleteOld ->
                            if (taskDeleteOld.isSuccessful) {
                                Log.e("Tag", "Task Delete is Succesfull")
                            }
                        }
                        userData.cover = downloadUrl
                    } else {
                        userData.cover = downloadUrl
                    }
                } else {
                    if (userData.profile.isNotBlank()) {
                        val profileOld = File(userData.profile)
                        val profileOldName = profileOld.nameWithoutExtension.drop(16)
                        val profileRef = storageRef.child(profileOldName + ".jpg").delete()
                        profileRef.addOnCompleteListener { taskDeleteOld ->
                            if (taskDeleteOld.isSuccessful) {
                                Log.e("Tag", "Task Delete is Succesfull")
                            }
                        }
                        userData.profile = downloadUrl
                    } else {
                        userData.profile = downloadUrl
                    }
                }
                isUpdateProfile = true
                dbRef.setValue(userData)
                progressDialog.dismiss()
            }
        }
    }
}