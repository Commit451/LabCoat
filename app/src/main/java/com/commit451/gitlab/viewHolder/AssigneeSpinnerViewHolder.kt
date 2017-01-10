package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Member
import com.commit451.gitlab.transformation.CircleTransformation
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

    @BindView(R.id.user_image) lateinit var image: ImageView
    @BindView(R.id.user_username) lateinit var textUsername: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(user: Member?) {
        if (user == null) {
            textUsername.setText(R.string.no_assignee)
            image.setImageResource(R.drawable.ic_assign_24dp)
        } else {
            textUsername.text = user.username
            App.get().picasso
                    .load(ImageUtil.getAvatarUrl(user, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size)))
                    .transform(CircleTransformation())
                    .into(image)
        }
    }
}
