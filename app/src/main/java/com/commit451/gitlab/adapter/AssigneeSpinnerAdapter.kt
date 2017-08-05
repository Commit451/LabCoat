package com.commit451.gitlab.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.viewHolder.AssigneeSpinnerViewHolder

/**
 * Adapter to show assignees in a spinner
 */
class AssigneeSpinnerAdapter(context: Context, members: MutableList<User?>) : ArrayAdapter<User?>(context, 0, members) {

    init {
        members.add(0, null)
        notifyDataSetChanged()
    }

    fun getSelectedItemPosition(userBasic: User?): Int {
        if (userBasic == null) {
            return 0
        }
        for (i in 0..count - 1) {
            val member = getItem(i)
            if (member != null && userBasic.id == member.id) {
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
        val member = getItem(position)
        val assigneeSpinnerViewHolder: AssigneeSpinnerViewHolder
        if (convertView == null) {
            assigneeSpinnerViewHolder = AssigneeSpinnerViewHolder.inflate(parent)
            assigneeSpinnerViewHolder.itemView.setTag(R.id.list_view_holder, assigneeSpinnerViewHolder)
        } else {
            assigneeSpinnerViewHolder = convertView.getTag(R.id.list_view_holder) as AssigneeSpinnerViewHolder
        }
        assigneeSpinnerViewHolder.bind(member)
        return assigneeSpinnerViewHolder.itemView
    }

}