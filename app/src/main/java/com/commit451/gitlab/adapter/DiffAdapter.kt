package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Diff
import com.commit451.gitlab.model.api.RepositoryCommit
import com.commit451.gitlab.viewHolder.DiffHeaderViewHolder
import com.commit451.gitlab.viewHolder.DiffViewHolder
import java.util.*

/**
 * Shows a bunch of diffs
 */
class DiffAdapter(private val repositoryCommit: RepositoryCommit, private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1

        private const val HEADER_COUNT = 1
    }

    private val values: ArrayList<Diff> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> return DiffHeaderViewHolder.inflate(parent)
            TYPE_ITEM -> {
                val holder = DiffViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onDiffClicked(getValueAt(position))
                }
                return holder
            }
        }
        throw IllegalStateException("No known view holder for $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DiffHeaderViewHolder) {
            holder.bind(repositoryCommit)
        } else if (holder is DiffViewHolder) {
            val diff = getValueAt(position)
            holder.bind(diff)
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
            return TYPE_ITEM
        }
    }

    fun getValueAt(position: Int): Diff {
        return values[position - HEADER_COUNT]
    }

    fun setData(diffs: Collection<Diff>?) {
        values.clear()
        if (diffs != null) {
            values.addAll(diffs)
        }
        notifyDataSetChanged()
    }

    interface Listener {
        fun onDiffClicked(diff: Diff)
    }
}
