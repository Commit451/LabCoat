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
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.ImageUtil

/**
 * Represents a merge request within a list
 */
class MergeRequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): MergeRequestViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_merge_request, parent, false)
            return MergeRequestViewHolder(view)
        }
    }

    @BindView(R.id.request_image)
    lateinit var image: ImageView
    @BindView(R.id.request_title)
    lateinit var textTitle: TextView
    @BindView(R.id.request_author)
    lateinit var textAuthor: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(item: MergeRequest) {
        App.get().picasso
                .load(ImageUtil.getAvatarUrl(item.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(image)

        if (item.author != null) {
            textAuthor.text = item.author!!.username
        } else {
            textAuthor.text = ""
        }

        if (item.title != null) {
            textTitle.text = item.title
        } else {
            textTitle.text = ""
        }
    }
}
