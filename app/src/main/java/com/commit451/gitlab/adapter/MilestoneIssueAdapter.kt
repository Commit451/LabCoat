package com.commit451.gitlab.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.viewHolder.IssueViewHolder
import com.commit451.gitlab.viewHolder.MilestoneHeaderViewHolder
import java.util.*

/**
 * Shows the issues associated with a [com.commit451.gitlab.model.api.Milestone]
 */
class MilestoneIssueAdapter(private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val TYPE_HEADER = 0
        private const val TYPE_MILESTONE = 1

        private const val HEADER_COUNT = 1
    }

    private val values: ArrayList<Issue> = ArrayList()
    private var milestone: Milestone? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> return MilestoneHeaderViewHolder.inflate(parent)
            TYPE_MILESTONE -> {
                val issueViewHolder = IssueViewHolder.inflate(parent)
                issueViewHolder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onIssueClicked(getValueAt(position))
                }
                return issueViewHolder
            }
        }
        throw IllegalStateException("No holder for viewType $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MilestoneHeaderViewHolder) {
            holder.bind(milestone!!)
        }
        if (holder is IssueViewHolder) {
            val issue = getValueAt(position)
            holder.bind(issue)
            holder.itemView.setTag(R.id.list_position, position)
        }
    }

    override fun getItemCount(): Int {
        return values.size + HEADER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER
        } else {
            return TYPE_MILESTONE
        }
    }

    fun getValueAt(position: Int): Issue {
        return values[position - HEADER_COUNT]
    }

    fun setIssues(issues: Collection<Issue>?) {
        values.clear()
        addIssues(issues)
    }

    fun addIssues(issues: Collection<Issue>?) {
        if (issues != null) {
            values.addAll(issues)
        }
        notifyDataSetChanged()
    }

    fun setMilestone(milestone: Milestone) {
        this.milestone = milestone
        notifyItemChanged(0)
    }

    fun addIssue(issue: Issue) {
        values.add(0, issue)
        notifyItemInserted(0)
    }

    fun updateIssue(issue: Issue) {
        var indexToDelete = -1
        for (i in values.indices) {
            if (values[i].id == issue.id) {
                indexToDelete = i
                break
            }
        }
        if (indexToDelete != -1) {
            values.removeAt(indexToDelete)
            values.add(indexToDelete, issue)
        }
        notifyItemChanged(indexToDelete)
    }

    interface Listener {
        fun onIssueClicked(issue: Issue)
    }
}
