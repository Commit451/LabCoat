package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Group

/**
 * View associated with a group
 */
class GroupViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): GroupViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user, parent, false)
            return GroupViewHolder(view)
        }
    }

    @BindView(R.id.image)
    lateinit var image: ImageView
    @BindView(R.id.name)
    lateinit var textName: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(group: Group) {
        textName.text = group.name

        if (!group.avatarUrl.isNullOrEmpty()) {
            App.get().picasso
                    .load(group.avatarUrl)
                    .into(image)
        }
    }
}
