package kuy.belajar.whatsappclone.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kuy.belajar.whatsappclone.R
import kuy.belajar.whatsappclone.model.User
import kuy.belajar.whatsappclone.recyclerview.UserSearchItemAdapter
import java.util.*

class SearchFragment : Fragment() {

    private lateinit var adapterRv: UserSearchItemAdapter
    private lateinit var userId: String
    private lateinit var dbRef: DatabaseReference
    private lateinit var allUsersListener: ValueEventListener
    private lateinit var textWatcher: TextWatcher
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
                    adapterRv.addUser(mUsers, false)
                    adapterRv.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(s.toString().toLowerCase(Locale.ROOT))
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        view.rv_search.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(view.context)
            adapter = adapterRv
        }
        return view
    }

    private fun searchForUsers(keyword: String) {
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("search")
            .startAt(keyword)
            .endAt(keyword + "\uf8ff")
        queryUsers.addListenerForSingleValueEvent(allUsersListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        dbRef = FirebaseDatabase.getInstance().reference.child("Users")
        dbRef.addValueEventListener(allUsersListener)

        keyword_search.addTextChangedListener(textWatcher)
    }

    override fun onDestroyView() {
        dbRef.removeEventListener(allUsersListener)
        keyword_search.removeTextChangedListener(textWatcher)
        super.onDestroyView()
    }
}