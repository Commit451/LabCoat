package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.util.DateUtil

/**
 * Header with information for milestones
 */
class MilestoneHeaderViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): MilestoneHeaderViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_milestone, parent, false)
            return MilestoneHeaderViewHolder(view)
        }
    }

    @BindView(R.id.description)
    lateinit var textDescription: TextView
    @BindView(R.id.due_date)
    lateinit var textDueDate: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(milestone: Milestone) {
        if (milestone.description != null) {
            textDescription.text = milestone.description
        }
        if (milestone.dueDate != null) {
            val due = DateUtil.getRelativeTimeSpanString(itemView.context, milestone.dueDate!!)
            textDueDate.text = String.format(itemView.resources.getString(R.string.due_date_formatted), due)
        }
    }
}
