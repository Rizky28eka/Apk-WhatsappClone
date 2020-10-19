package kuy.belajar.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kuy.belajar.whatsappclone.fragments.ChatFragment
import kuy.belajar.whatsappclone.fragments.SearchFragment
import kuy.belajar.whatsappclone.fragments.SettingsFragment
import kuy.belajar.whatsappclone.model.User

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var userId: String
    private lateinit var userCurrent: User
    private lateinit var userListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(ChatFragment(), "Chats")
        viewPagerAdapter.addFragment(SearchFragment(), "Search")
        viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

        view_pager.adapter = viewPagerAdapter
        tab_layout.setupWithViewPager(view_pager)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userCurrent = snapshot.getValue(User::class.java) as User
                    user_name.text = userCurrent.username
                    if (userCurrent.profile.isNotBlank()) Picasso.get().load(userCurrent.profile)
                        .placeholder(R.drawable.ic_profile).into(profile_image)
                } else {
                    mAuth.signOut()
                    val intentWelcome = Intent(this@MainActivity, WelcomeActivity::class.java)
                    intentWelcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intentWelcome)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        mAuth = FirebaseAuth.getInstance()
        userId = mAuth.currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addValueEventListener(userListener)
    }

    override fun onStart() {
        super.onStart()
        userRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usr = snapshot.getValue(User::class.java) as User
                    val userUpdate = usr.copy(status = "online")
                    userRef.setValue(userUpdate)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    override fun onStop() {
        super.onStop()
        userRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val usr = snapshot.getValue(User::class.java) as User
                    val userUpdate = usr.copy(status = "offline")
                    userRef.setValue(userUpdate)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        userRef.removeEventListener(userListener)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                mAuth.signOut()
                val intentWelcome = Intent(this, WelcomeActivity::class.java)
                intentWelcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentWelcome)
                toast("User berhasil Logout")
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = arrayListOf<Fragment>()
        private val titles = arrayListOf<String>()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }
}