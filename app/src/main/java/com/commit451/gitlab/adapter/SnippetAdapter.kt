package com.commit451.gitlab.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Snippet
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.SnippetViewHolder
import java.util.*


class SnippetAdapter(private val listener: SnippetAdapter.Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private val FOOTER_COUNT = 1

        private val TYPE_ITEM = 0
        private val TYPE_FOOTER = 1
    }

    private val values: MutableList<Snippet> = ArrayList()
    private var loading: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = SnippetViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onSnippetClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No holder for viewType " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SnippetViewHolder) {
            val snippet = getValueAt(position)
            holder.bind(snippet)
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

    fun getValueAt(position: Int): Snippet {
        return values[position]
    }

    fun setData(milestones: Collection<Snippet>?) {
        values.clear()
        addData(milestones)
    }

    fun addData(milestones: Collection<Snippet>?) {
        if (milestones != null) {
            values.addAll(milestones)
        }
        notifyDataSetChanged()
    }

    fun addSnippet(milestone: Snippet) {
        values.add(0, milestone)
        notifyItemInserted(0)
    }

    fun updateIssue(snippet: Snippet) {
        var indexToDelete = -1
        for (i in values.indices) {
            if (values[i].id == snippet.id) {
                indexToDelete = i
                break
            }
        }
        if (indexToDelete != -1) {
            values.removeAt(indexToDelete)
            values.add(indexToDelete, snippet)
        }
        notifyItemChanged(indexToDelete)
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onSnippetClicked(snippet: Snippet)
    }
}