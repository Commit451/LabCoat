package com.commit451.gitlab.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.viewHolder.GroupViewHolder
import java.util.*

/**
 * All the groups
 */
class GroupAdapter(private val listener: GroupAdapter.Listener) : RecyclerView.Adapter<GroupViewHolder>() {

    val values: ArrayList<Group> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val holder = GroupViewHolder.inflate(parent)
        holder.itemView.setOnClickListener { v ->
            val position = v.getTag(R.id.list_position) as Int
            listener.onGroupClicked(getEntry(position), holder)
        }
        return holder
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.itemView.setTag(R.id.list_position, position)
        holder.itemView.setTag(R.id.list_view_holder, holder)
        holder.bind(getEntry(position))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setGroups(groups: Collection<Group>) {
        values.clear()
        addGroups(groups)
    }

    fun addGroups(groups: Collection<Group>?) {
        if (groups != null) {
            values.addAll(groups)
        }
        notifyDataSetChanged()
    }

    private fun getEntry(position: Int): Group {
        return values[position]
    }

    interface Listener {
        fun onGroupClicked(group: Group, groupViewHolder: GroupViewHolder)
    }
}
