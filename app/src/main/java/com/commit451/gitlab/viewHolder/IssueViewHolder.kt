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
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil

/**
 * issues, yay!
 */
class IssueViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): IssueViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_issue, parent, false)
            return IssueViewHolder(view)
        }
    }

    @BindView(R.id.issue_state)
    lateinit var textState: TextView
    @BindView(R.id.issue_image)
    lateinit var image: ImageView
    @BindView(R.id.issue_message)
    lateinit var textMessage: TextView
    @BindView(R.id.issue_creator)
    lateinit var textCreator: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(issue: Issue) {

        when (issue.state) {
            Issue.STATE_OPENED -> textState.text = itemView.resources.getString(R.string.issue_open)
            Issue.STATE_CLOSED -> textState.text = itemView.resources.getString(R.string.issue_closed)
            else -> textState.visibility = View.GONE
        }

        if (issue.assignee != null) {
            App.get().picasso
                    .load(ImageUtil.getAvatarUrl(issue.assignee, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                    .transform(CircleTransformation())
                    .into(image)
        } else {
            image.setImageBitmap(null)
        }

        textMessage.text = issue.title
        textMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        if (issue.isConfidential) {
            textMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility_off_white_24dp, 0, 0, 0)
        }

        var time = ""
        if (issue.createdAt != null) {
            time += DateUtil.getRelativeTimeSpanString(itemView.context, issue.createdAt)
        }
        var author = ""
        if (issue.author != null) {
            author += issue.author!!.username
        }
        val id: String
        var issueId = issue.iid
        if (issueId < 1) {
            issueId = issue.id
        }
        id = "#" + issueId

        textCreator.text = String.format(itemView.context.getString(R.string.opened_time), id, time, author)
    }
}
