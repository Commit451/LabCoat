package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
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

    @BindView(R.id.commit_image) lateinit var image: ImageView
    @BindView(R.id.commit_message) lateinit var textMessage: TextView
    @BindView(R.id.commit_author) lateinit var textAuthor: TextView
    @BindView(R.id.commit_time) lateinit var textTime: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(commit: RepositoryCommit) {
        Picasso.with(itemView.context)
                .load(ImageUtil.getAvatarUrl(commit.authorEmail, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(image)

        textMessage.text = commit.title
        textAuthor.text = commit.authorName
        if (commit.createdAt != null) {
            textTime.text = DateUtil.getRelativeTimeSpanString(itemView.context, commit.createdAt)
        } else {
            textTime.setText(R.string.unknown)
        }
    }
}
