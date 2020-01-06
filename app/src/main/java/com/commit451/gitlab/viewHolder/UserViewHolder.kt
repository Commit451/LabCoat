package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.util.ImageUtil

/**
 * Shows a single user
 */
class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user, parent, false)
            return UserViewHolder(view)
        }
    }

    private val textUsername: TextView by bindView(R.id.name)
    val image: ImageView by bindView(R.id.image)

    fun bind(user: User) {
        textUsername.text = user.username
        image.load(ImageUtil.getAvatarUrl(user, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size)))
    }
}
