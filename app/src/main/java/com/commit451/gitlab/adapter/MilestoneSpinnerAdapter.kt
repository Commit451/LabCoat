package com.commit451.gitlab.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.viewHolder.MilestoneSpinnerViewHolder

/**
 * Adapter to show assignees in a spinner
 */
class MilestoneSpinnerAdapter(context: Context, milestones: MutableList<Milestone?>) : ArrayAdapter<Milestone>(context, 0, milestones) {

    init {
        milestones.add(0, null)
        notifyDataSetChanged()
    }

    fun getSelectedItemPosition(currentMilestone: Milestone?): Int {
        if (currentMilestone == null) {
            return 0
        }
        for (i in 0..count - 1) {
            val milestone = getItem(i)
            if (milestone != null && currentMilestone.id == milestone.id) {
                return i
            }
        }
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getTheView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getTheView(position, convertView, parent)
    }

    private fun getTheView(position: Int, convertView: View?, parent: ViewGroup): View {
        val milestone = getItem(position)
        val milestoneSpinnerViewHolder: MilestoneSpinnerViewHolder
        if (convertView == null) {
            milestoneSpinnerViewHolder = MilestoneSpinnerViewHolder.inflate(parent)
            milestoneSpinnerViewHolder.itemView.setTag(R.id.list_view_holder, milestoneSpinnerViewHolder)
        } else {
            milestoneSpinnerViewHolder = convertView.getTag(R.id.list_view_holder) as MilestoneSpinnerViewHolder
        }
        milestoneSpinnerViewHolder.bind(milestone)
        return milestoneSpinnerViewHolder.itemView
    }

}