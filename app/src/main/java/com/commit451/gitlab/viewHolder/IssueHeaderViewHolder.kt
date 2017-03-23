package com.commit451.gitlab.viewHolder

import `in`.uncod.android.bypass.Bypass
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
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.BypassImageGetterFactory
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.vdurmont.emoji.EmojiParser

/**
 * Header for an issue
 */
class IssueHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): IssueHeaderViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_issue, parent, false)
            return IssueHeaderViewHolder(view)
        }
    }

    @BindView(R.id.description) lateinit var textDescription: TextView
    @BindView(R.id.author_image) lateinit var imageAuthor: ImageView
    @BindView(R.id.author) lateinit var textAuthor: TextView
    @BindView(R.id.milestone_root) lateinit var rootMilestone: ViewGroup
    @BindView(R.id.milestone_text) lateinit var textMilestone: TextView

    val bypass: Bypass = Bypass(view.context)

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(issue: Issue, project: Project) {

        if (issue.description.isNullOrEmpty()) {
            textDescription.visibility = View.GONE
        } else {
            textDescription.visibility = View.VISIBLE
            val getter = BypassImageGetterFactory.create(textDescription,
                    App.get().picasso,
                    App.get().getAccount().serverUrl.toString(),
                    project)
            var description = issue.description
            description = EmojiParser.parseToUnicode(description)
            textDescription.text = bypass.markdownToSpannable(description, getter)
            textDescription.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl)
        }

        App.get().picasso
                .load(ImageUtil.getAvatarUrl(issue.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(imageAuthor)

        var author = ""
        if (issue.author != null) {
            author = issue.author.name + " "
        }
        author += itemView.resources.getString(R.string.created_issue)
        if (issue.createdAt != null) {
            author = author + " " + DateUtil.getRelativeTimeSpanString(itemView.context, issue.createdAt)
        }
        textAuthor.text = author
        if (issue.milestone != null) {
            rootMilestone.visibility = View.VISIBLE
            textMilestone.text = issue.milestone.title
        } else {
            rootMilestone.visibility = View.GONE
        }
    }
}