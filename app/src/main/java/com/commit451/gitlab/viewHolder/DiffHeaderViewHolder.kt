package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.squareup.picasso.Picasso

/**
 * Header that gives the details of a merge request
 */
class DiffHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): DiffHeaderViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_diff, parent, false)
            return DiffHeaderViewHolder(view)
        }
    }

    @BindView(R.id.commit_author_image) lateinit var image: ImageView
    @BindView(R.id.commit_author) lateinit var textAuthor: TextView
    @BindView(R.id.commit_time) lateinit var textTime: TextView
    @BindView(R.id.commit_title) lateinit var textTitle: TextView
    @BindView(R.id.commit_message) lateinit var textMessage: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(commit: RepositoryCommit) {
        Picasso.with(itemView.context)
                .load(ImageUtil.getAvatarUrl(commit.authorEmail, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(image)

        textAuthor.text = commit.authorName
        if (commit.createdAt == null) {
            textTime.text = null
        } else {
            textTime.text = DateUtil.getRelativeTimeSpanString(itemView.context, commit.createdAt)
        }

        textTitle.text = commit.title
        val message = extractMessage(commit.title, commit.message)
        textMessage.text = message
        textMessage.visibility = if (message.isEmpty()) View.GONE else View.VISIBLE
    }

    /**
     * This extracts the trailing part of the textTitle as it is displayed in the GitLabService web interface
     * (the commit message also contains the commit textTitle)
     */
    private fun extractMessage(title: String, message: String): String {
        if (!TextUtils.isEmpty(message)) {
            val ellipsis = title.endsWith("\u2026") && message[title.length - 1] != '\u2026'
            val trailing = message.substring(title.length - if (ellipsis) 1 else 0)
            return if (trailing == "\u2026") "" else ((if (ellipsis) "\u2026" else "") + trailing).trim { it <= ' ' }
        }
        return title
    }
}
