package kuy.belajar.whatsappclone.recyclerview

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kuy.belajar.whatsappclone.MessageChatActivity
import kuy.belajar.whatsappclone.R
import kuy.belajar.whatsappclone.model.User

class UserSearchItemAdapter : RecyclerView.Adapter<UserSearchItemVH>() {
    private val listUser = arrayListOf<User>()
    private var isChatChecked = false

    fun addUser(users: List<User>, isChat: Boolean) {
        listUser.clear()
        listUser.addAll(users)
        isChatChecked = isChat
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchItemVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_search_item, parent, false)
        return UserSearchItemVH(view)
    }

    override fun onBindViewHolder(holder: UserSearchItemVH, position: Int) {
        val data = listUser[position]
        holder.bindData(data)
        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>("Send Message", "Visit Profile")
            val alertBuilder = AlertDialog.Builder(it.context)
            alertBuilder.run {
                setTitle("What Do You Want ?")
                setItems(options) { dialog, position ->
                    when (position) {
                        0 -> {
                            val intentMessage = Intent(it.context, MessageChatActivity::class.java)
                            intentMessage.putExtra("visit_id", data.uid)
                            it.context.startActivity(intentMessage)
                            dialog.dismiss()
                        }
                        else -> {
                            dialog.cancel()
                        }
                    }
                }
                show()
            }
        }
    }

    override fun getItemCount(): Int = listUser.size
}