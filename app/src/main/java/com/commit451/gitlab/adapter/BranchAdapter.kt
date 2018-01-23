package com.commit451.gitlab.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Branch
import com.commit451.gitlab.viewHolder.BranchViewHolder
import java.util.*

/**
 * Adapts the feeds
 */
class BranchAdapter(private val ref: Ref?, private val listener: BranchAdapter.Listener) : RecyclerView.Adapter<BranchViewHolder>() {

    private val values: ArrayList<Branch> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val holder = BranchViewHolder.inflate(parent)
        holder.itemView.setOnClickListener { v ->
            val position = v.getTag(R.id.list_position) as Int
            listener.onBranchClicked(getEntry(position))
        }
        return holder
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        holder.itemView.setTag(R.id.list_position, position)
        val branch = getEntry(position)
        var selected = false
        if (ref != null) {
            if (ref.type == Ref.TYPE_BRANCH && ref.ref == branch.name) {
                selected = true
            }
        }
        holder.bind(branch, selected)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setEntries(entries: Collection<Branch>?) {
        values.clear()
        if (entries != null) {
            values.addAll(entries)
        }
        notifyDataSetChanged()
    }

    fun addEntries(entries: Collection<Branch>) {
        if (!entries.isEmpty()) {
            val start = values.size
            this.values.addAll(entries)
            notifyItemRangeChanged(start, this.values.size)
        }
    }

    private fun getEntry(position: Int): Branch {
        return values[position]
    }

    interface Listener {
        fun onBranchClicked(entry: Branch)
    }
}
