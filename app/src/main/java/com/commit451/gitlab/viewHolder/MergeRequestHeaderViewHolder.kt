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
import com.commit451.gitlab.extension.setMarkdownText
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod

/**
 * Header that gives the details of a merge request
 */
class MergeRequestHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): MergeRequestHeaderViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_merge_request, parent, false)
            return MergeRequestHeaderViewHolder(view)
        }
    }

    @BindView(R.id.description) lateinit var textDescription: TextView
    @BindView(R.id.author_image) lateinit var imageAuthor: ImageView
    @BindView(R.id.author) lateinit var textAuthor: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(mergeRequest: MergeRequest, project: Project) {
        if (mergeRequest.description.isNullOrEmpty()) {
            textDescription.visibility = View.GONE
        } else {
            textDescription.visibility = View.VISIBLE
            textDescription.setMarkdownText(mergeRequest.description, project)
            textDescription.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl)
        }

        App.get().picasso
                .load(ImageUtil.getAvatarUrl(mergeRequest.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(imageAuthor)

        var author = ""
        if (mergeRequest.author != null) {
            author += mergeRequest.author.name + " "
        }
        author += itemView.resources.getString(R.string.created_merge_request)
        if (mergeRequest.createdAt != null) {
            author += " " + DateUtil.getRelativeTimeSpanString(itemView.context, mergeRequest.createdAt)
        }
        textAuthor.text = author
    }
}
