package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.util.DateUtil

/**
 * Milestone
 */
class MilestoneViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): MilestoneViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_milestone, parent, false)
            return MilestoneViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.title)
    private val textDueDate: TextView by bindView(R.id.due_date)

    fun bind(milestone: Milestone) {
        textTitle.text = milestone.title
        if (milestone.dueDate != null) {
            textDueDate.visibility = View.VISIBLE
            val due = DateUtil.getRelativeTimeSpanString(itemView.context, milestone.dueDate!!)
            textDueDate.text = String.format(itemView.resources.getString(R.string.due_date_formatted), due)
        } else {
            textDueDate.visibility = View.GONE
            textDueDate.text = ""
        }
    }
}
