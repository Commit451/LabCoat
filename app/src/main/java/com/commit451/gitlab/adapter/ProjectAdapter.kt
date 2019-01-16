package com.commit451.gitlab.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.ProjectViewHolder
import java.util.*

/**
 * Shows a list of projects
 */
class ProjectAdapter(context: Context, private val listener: ProjectAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {

        val FOOTER_COUNT = 1

        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1
    }

    private val values: MutableList<Project> = ArrayList()
    private val colors: IntArray = context.resources.getIntArray(R.array.cool_colors)
    private var loading: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = ProjectViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onProjectClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No idea what to create for view type " + viewType)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder is ProjectViewHolder) {
            val project = getValueAt(position)
            holder.bind(project, colors[position % colors.size])
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

    fun getValueAt(position: Int): Project {
        return values[position]
    }

    fun clearData() {
        values.clear()
        notifyDataSetChanged()
    }

    fun setData(projects: Collection<Project>?) {
        values.clear()
        if (projects != null) {
            values.addAll(projects)
        }
        notifyDataSetChanged()
    }

    fun addData(projects: Collection<Project>) {
        values.addAll(projects)
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onProjectClicked(project: Project)
    }
}
