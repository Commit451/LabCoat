package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.MilestoneViewHolder
import java.util.*


class MilestoneAdapter(private val listener: MilestoneAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {

        val FOOTER_COUNT = 1

        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1
    }

    val values: MutableList<Milestone> = ArrayList()
    private var loading: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = MilestoneViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onMilestoneClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No holder for viewType " + viewType)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder is MilestoneViewHolder) {
            val milestone = getValueAt(position)
            holder.bind(milestone)
            holder.itemView.setTag(R.id.list_position, position)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_ITEM
        }
    }

    fun getValueAt(position: Int): Milestone {
        return values[position]
    }

    fun setData(milestones: Collection<Milestone>?) {
        values.clear()
        addData(milestones)
    }

    fun addData(milestones: Collection<Milestone>?) {
        if (milestones != null) {
            values.addAll(milestones)
        }
        notifyDataSetChanged()
    }

    fun addMilestone(milestone: Milestone) {
        values.add(0, milestone)
        notifyItemInserted(0)
    }

    fun updateIssue(milestone: Milestone) {
        var indexToDelete = -1
        for (i in values.indices) {
            if (values[i].id == milestone.id) {
                indexToDelete = i
                break
            }
        }
        if (indexToDelete != -1) {
            values.removeAt(indexToDelete)
            values.add(indexToDelete, milestone)
        }
        notifyItemChanged(indexToDelete)
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onMilestoneClicked(milestone: Milestone)
    }
}