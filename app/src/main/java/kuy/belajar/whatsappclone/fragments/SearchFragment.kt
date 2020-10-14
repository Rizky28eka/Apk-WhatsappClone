package kuy.belajar.whatsappclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kuy.belajar.whatsappclone.R
import kuy.belajar.whatsappclone.model.User
import kuy.belajar.whatsappclone.recyclerview.UserSearchItemAdapter

class SearchFragment : Fragment() {

    private lateinit var adapterRv: UserSearchItemAdapter
    private lateinit var userId: String
    private lateinit var dbRef: DatabaseReference
    private lateinit var allUsersListener: ValueEventListener
    private var mUsers = arrayListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        adapterRv = UserSearchItemAdapter()

        allUsersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0) {
                    mUsers.clear()
                    snapshot.children.forEach {
                        val user = it.getValue(User::class.java) as User
                        if (user.uid != userId) mUsers.add(user)
                    }
                    adapterRv.addUser(mUsers)
                    adapterRv.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        view.rv_search.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(view.context)
            adapter = adapterRv
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        dbRef = FirebaseDatabase.getInstance().reference.child("Users")
        dbRef.addValueEventListener(allUsersListener)
    }

    override fun onDestroyView() {
        dbRef.removeEventListener(allUsersListener)
        super.onDestroyView()
    }
}