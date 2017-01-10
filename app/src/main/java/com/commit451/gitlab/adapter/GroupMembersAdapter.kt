package com.commit451.gitlab.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.PopupMenu
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Member
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder
import java.util.*

/**
 * Adapter for a list of users
 */
class GroupMembersAdapter(private val listener: GroupMembersAdapter.Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private val TYPE_MEMBER = 0
        private val TYPE_FOOTER = 1

        private val FOOTER_COUNT = 1
    }

    val values: ArrayList<Member> = ArrayList()

    var isLoading = false
        set(loading) {
            field = loading
            notifyItemChanged(values.size)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_MEMBER -> {
                val holder = ProjectMemberViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onUserClicked(getMember(position), holder)
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No known ViewHolder for type " + viewType)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProjectMemberViewHolder) {
            val member = values[position]
            holder.bind(member)
            holder.popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_change_access -> {
                        listener.onUserChangeAccessClicked(member)
                        return@OnMenuItemClickListener true
                    }
                    R.id.action_remove -> {
                        listener.onUserRemoveClicked(member)
                        return@OnMenuItemClickListener true
                    }
                }
                false
            })
            holder.itemView.setTag(R.id.list_position, position)
            holder.itemView.setTag(R.id.list_view_holder, holder)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(isLoading)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_MEMBER
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    fun setData(members: Collection<Member>?) {
        values.clear()
        addData(members)
    }

    fun addData(members: Collection<Member>?) {
        if (members != null) {
            values.addAll(members)
        }
        notifyDataSetChanged()
    }

    fun addMember(member: Member) {
        values.add(0, member)
        notifyItemInserted(0)
    }

    fun removeMember(member: Member) {
        val index = values.indexOf(member)
        values.removeAt(index)
        notifyItemRemoved(index)
    }

    fun isFooter(position: Int): Boolean {
        val viewType = getItemViewType(position)
        if (viewType == TYPE_FOOTER) {
            return true
        }
        return false
    }

    private fun getMember(position: Int): Member {
        return values[position]
    }

    interface Listener {
        fun onUserClicked(member: Member, userViewHolder: ProjectMemberViewHolder)
        fun onUserRemoveClicked(member: Member)
        fun onUserChangeAccessClicked(member: Member)
    }
}
