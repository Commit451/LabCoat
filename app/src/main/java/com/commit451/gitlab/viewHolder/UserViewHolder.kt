package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.load
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.util.ImageUtil
import com.github.ivbaranov.mli.MaterialLetterIcon

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

    @BindView(R.id.name)
    lateinit var textUsername: TextView
    @BindView(R.id.image)
    lateinit var image: ImageView
    @BindView(R.id.letter)
    lateinit var iconLetter: MaterialLetterIcon

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(user: User) {
        textUsername.text = user.username
        image.load(ImageUtil.getAvatarUrl(user, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size)))
    }
}
