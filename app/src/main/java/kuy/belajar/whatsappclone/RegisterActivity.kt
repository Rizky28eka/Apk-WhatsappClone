package kuy.belajar.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import kuy.belajar.whatsappclone.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener {
            val username = input_username.text.toString()
            val email = input_email.text.toString()
            val password = input_password.text.toString()
            registerUser(username, email, password)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun registerUser(username: String, email: String, password: String) {
        if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    userId = mAuth.currentUser?.uid.toString()
                    dbRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

                    val newUser = User(uid = userId, username = username)
                    dbRef.setValue(newUser).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intentMain = Intent(this, MainActivity::class.java)
                            intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intentMain)
                            toast("User baru telah dibuat.")
                            finish()
                        } else {
                            it.exception?.message?.let {
                                toast("Gagal mendaftarkan User.\nError : $it")
                            }
                        }
                    }
                } else {
                    it.exception?.message?.let {
                        toast("Gagal mendaftarkan User.\nError : $it")
                    }
                }
            }
            return
        }

        val message =
            if (username.isBlank()) "Username " else if (email.isBlank()) "Email" else "Password "
        toast("$message tidak boleh kosong")
    }
}