package com.commit451.gitlab.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Label
import com.commit451.gitlab.viewHolder.LabelViewHolder
import com.commit451.gitlab.viewHolder.ProjectMemberFooterViewHolder
import java.util.*

/**
 * Shows a bunch of labels
 */
class LabelAdapter(private val listener: LabelAdapter.Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private val TYPE_ITEM = 0
    }

    val items: ArrayList<Label> = ArrayList()

    fun getItem(position: Int): Label {
        return items[position]
    }

    fun setItems(data: Collection<Label>?) {
        items.clear()
        if (data != null) {
            items.addAll(data)
        }
        notifyDataSetChanged()
    }

    fun addLabel(label: Label) {
        items.add(0, label)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = LabelViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onLabelClicked(getItem(position), holder)
                }
                return holder
            }
        }
        throw IllegalStateException("No idea what to inflate with view type of " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProjectMemberFooterViewHolder) {
            //
        } else if (holder is LabelViewHolder) {
            val label = getItem(position)
            holder.bind(label)
            holder.itemView.setTag(R.id.list_position, position)
            holder.itemView.setTag(R.id.list_view_holder, holder)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_ITEM
    }

    interface Listener {
        fun onLabelClicked(label: Label, viewHolder: LabelViewHolder)
    }
}
