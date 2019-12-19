package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.viewHolder.CommitViewHolder
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import java.util.*

/**
 * Shows a list of commits to a project, seen in a project overview
 */
class CommitAdapter(private val listener: CommitAdapter.Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private val FOOTER_COUNT = 1

        private val TYPE_ITEM = 0
        private val TYPE_FOOTER = 1
    }

    private val values: ArrayList<RepositoryCommit> = ArrayList()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = CommitViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onCommitClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No known ViewHolder for type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommitViewHolder) {
            val commit = getValueAt(position)
            holder.bind(commit)
            holder.itemView.setTag(R.id.list_position, position)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == values.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    fun getValueAt(position: Int): RepositoryCommit {
        return values[position]
    }

    fun addData(commits: Collection<RepositoryCommit>?) {
        if (commits != null) {
            values.addAll(commits)
            notifyItemRangeInserted(0, commits.size)
        }
        notifyDataSetChanged()
    }

    fun setData(commits: Collection<RepositoryCommit>?) {
        values.clear()
        addData(commits)
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onCommitClicked(commit: RepositoryCommit)
    }
}

