package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import coil.api.load
import coil.transform.CircleCropTransformation
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.util.ImageUtil

/**
 * Shows assignee in a spinner
 */
class AssigneeSpinnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): AssigneeSpinnerViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_assignee, parent, false)
            return AssigneeSpinnerViewHolder(view)
        }
    }

    @BindView(R.id.user_image)
    lateinit var image: ImageView
    @BindView(R.id.user_username)
    lateinit var textUsername: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(user: User?) {
        if (user == null) {
            textUsername.setText(R.string.no_assignee)
            image.setImageResource(R.drawable.ic_assign_24dp)
        } else {
            textUsername.text = user.username
            image.load(ImageUtil.getAvatarUrl(user, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size))) {
                transformations(CircleCropTransformation())
            }
        }
    }
}
