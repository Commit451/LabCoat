package com.commit451.gitlab.adapter

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.ProjectNamespace
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.viewHolder.ProjectMemberFooterViewHolder
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder
import java.util.*

/**
 * Shows a projects members and a groups members
 */
class ProjectMembersAdapter(private val listener: ProjectMembersAdapter.Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        val TYPE_MEMBER = 0
        val TYPE_FOOTER = 1

        val FOOTER_COUNT = 1
    }

    private val members: ArrayList<User> = ArrayList()
    private var namespace: ProjectNamespace? = null

    val spanSizeLookup: GridLayoutManager.SpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val viewType = getItemViewType(position)
            if (viewType == TYPE_FOOTER) {
                return 2
            } else {
                return 1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_MEMBER -> {
                val projectViewHolder = ProjectMemberViewHolder.inflate(parent)
                projectViewHolder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    val memberGroupViewHolder = v.getTag(R.id.list_view_holder) as ProjectMemberViewHolder
                    listener.onProjectMemberClicked(getProjectMember(position), memberGroupViewHolder)
                }
                return projectViewHolder
            }
            TYPE_FOOTER -> {
                val footerHolder = ProjectMemberFooterViewHolder.inflate(parent)
                footerHolder.itemView.setOnClickListener { listener.onSeeGroupClicked() }
                return footerHolder
            }
        }
        throw IllegalStateException("No idea what to inflate with view type of " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProjectMemberFooterViewHolder) {
            if (namespace == null) {
                holder.itemView.visibility = View.GONE
            } else {
                holder.itemView.visibility = View.VISIBLE
                holder.bind(namespace!!)
            }
        } else if (holder is ProjectMemberViewHolder) {
            val member = getProjectMember(position)
            holder.bind(member)
            holder.itemView.setTag(R.id.list_position, position)
            holder.itemView.setTag(R.id.list_view_holder, holder)
            holder.popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_change_access -> {
                        listener.onChangeAccess(member)
                        return@OnMenuItemClickListener true
                    }
                    R.id.action_remove -> {
                        listener.onRemoveMember(member)
                        return@OnMenuItemClickListener true
                    }
                }
                false
            })
        }
    }

    override fun getItemCount(): Int {
        return members.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        if (position == members.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_MEMBER
        }
    }

    fun setProjectMembers(data: Collection<User>?) {
        members.clear()
        addProjectMembers(data)
    }

    fun addProjectMembers(data: Collection<User>?) {
        if (data != null) {
            members.addAll(data)
        }
        notifyDataSetChanged()
    }

    fun setNamespace(namespace: ProjectNamespace?) {
        this.namespace = namespace
        notifyDataSetChanged()
    }

    fun getProjectMember(position: Int): User {
        return members[position]
    }

    fun addMember(member: User) {
        members.add(0, member)
        notifyItemInserted(0)
    }

    fun removeMember(member: User) {
        val position = members.indexOf(member)
        members.remove(member)
        notifyItemRemoved(position)
    }

    interface Listener {
        fun onProjectMemberClicked(member: User, memberGroupViewHolder: ProjectMemberViewHolder)
        fun onRemoveMember(member: User)
        fun onChangeAccess(member: User)
        fun onSeeGroupClicked()
    }
}
