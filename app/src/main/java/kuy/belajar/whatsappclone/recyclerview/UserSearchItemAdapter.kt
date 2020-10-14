package kuy.belajar.whatsappclone.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kuy.belajar.whatsappclone.R
import kuy.belajar.whatsappclone.model.User

class UserSearchItemAdapter : RecyclerView.Adapter<UserSearchItemVH>() {
    private val listUser = arrayListOf<User>()

    fun addUser(users: List<User>) {
        listUser.clear()
        listUser.addAll(users)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchItemVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_search_item, parent, false)
        return UserSearchItemVH(view)
    }

    override fun onBindViewHolder(holder: UserSearchItemVH, position: Int) {
        val data = listUser[position]
        holder.bindData(data)
    }

    override fun getItemCount(): Int = listUser.size
}