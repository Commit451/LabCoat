package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.MergeRequest
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

    private val image: ImageView by bindView(R.id.request_image)
    private val textTitle: TextView by bindView(R.id.request_title)
    private val textAuthor: TextView by bindView(R.id.request_author)

    fun bind(item: MergeRequest) {
        image.load(ImageUtil.getAvatarUrl(item.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size))) {
            transformations(CircleCropTransformation())
        }

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
