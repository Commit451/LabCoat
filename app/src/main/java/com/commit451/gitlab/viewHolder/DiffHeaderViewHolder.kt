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
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil

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

    private val image: ImageView by bindView(R.id.commit_author_image)
    private val textAuthor: TextView by bindView(R.id.commit_author)
    private val textTime: TextView by bindView(R.id.commit_time)
    private val textTitle: TextView by bindView(R.id.commit_title)
    private val textMessage: TextView by bindView(R.id.commit_message)

    fun bind(commit: RepositoryCommit) {
        image.load(ImageUtil.getAvatarUrl(commit.authorEmail, itemView.resources.getDimensionPixelSize(R.dimen.image_size))) {
            transformations(CircleCropTransformation())
        }

        textAuthor.text = commit.authorName
        if (commit.createdAt == null) {
            textTime.text = null
        } else {
            textTime.text = DateUtil.getRelativeTimeSpanString(itemView.context, commit.createdAt)
        }

        textTitle.text = commit.title
        val message = extractMessage(commit.title!!, commit.message)
        textMessage.text = message
        textMessage.visibility = if (message.isEmpty()) View.GONE else View.VISIBLE
    }

    /**
     * This extracts the trailing part of the textTitle as it is displayed in the GitLabService web interface
     * (the commit message also contains the commit textTitle)
     */
    private fun extractMessage(title: String, message: String?): String {
        if (!message.isNullOrEmpty()) {
            val ellipsis = title.endsWith("\u2026") && message[title.length - 1] != '\u2026'
            val trailing = message.substring(title.length - if (ellipsis) 1 else 0)
            return if (trailing == "\u2026") "" else ((if (ellipsis) "\u2026" else "") + trailing).trim { it <= ' ' }
        }
        return title
    }
}
