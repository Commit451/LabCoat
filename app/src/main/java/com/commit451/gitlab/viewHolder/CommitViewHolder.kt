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
 * Shows a commit
 */
class CommitViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): CommitViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_commit, parent, false)
            return CommitViewHolder(view)
        }
    }

    private val image: ImageView by bindView(R.id.commit_image)
    private val textMessage: TextView by bindView(R.id.commit_message)
    private val textAuthor: TextView by bindView(R.id.commit_author)
    private val textTime: TextView by bindView(R.id.commit_time)

    fun bind(commit: RepositoryCommit) {
        image.load(ImageUtil.getAvatarUrl(commit.authorEmail, itemView.resources.getDimensionPixelSize(R.dimen.image_size))) {
            transformations(CircleCropTransformation())
        }

        textMessage.text = commit.title
        textAuthor.text = commit.authorName
        if (commit.createdAt != null) {
            textTime.text = DateUtil.getRelativeTimeSpanString(itemView.context, commit.createdAt)
        } else {
            textTime.setText(R.string.unknown)
        }
    }
}
