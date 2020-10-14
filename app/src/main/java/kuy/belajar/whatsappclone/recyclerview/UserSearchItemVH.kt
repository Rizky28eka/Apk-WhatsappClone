package kuy.belajar.whatsappclone.recyclerview

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_search_item.view.*
import kuy.belajar.whatsappclone.R
import kuy.belajar.whatsappclone.model.User


class UserSearchItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindData(user: User) {
        with(itemView) {
            if (user.profile.isNotBlank()) Picasso.get().load(user.profile)
                .placeholder(R.drawable.ic_profile).into(profile_image)
            username.text = user.username

            val colorOffline = if (user.status == "online") ContextCompat.getDrawable(
                itemView.context,
                R.drawable.green_light
            ) else ContextCompat.getDrawable(itemView.context, R.drawable.darker_gray)
            status_online.setImageDrawable(colorOffline)
        }
    }
}
