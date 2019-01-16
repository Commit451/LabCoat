package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.Tag
import com.commit451.gitlab.viewHolder.TagViewHolder
import java.util.*

/**
 * Tags
 */
class TagAdapter(val ref: Ref?, val listener: TagAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<TagViewHolder>() {

    private val values: ArrayList<Tag> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val holder = TagViewHolder.inflate(parent)
        holder.itemView.setOnClickListener { v ->
            val position = v.getTag(R.id.list_position) as Int
            listener.onTagClicked(getEntry(position))
        }
        return holder
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.itemView.setTag(R.id.list_position, position)
        val tag = getEntry(position)
        var selected = false
        if (ref != null) {
            if (ref.type == Ref.TYPE_TAG && ref.ref == tag.name) {
                selected = true
            }
        }
        holder.bind(tag, selected)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setEntries(entries: Collection<Tag>?) {
        values.clear()
        if (entries != null) {
            values.addAll(entries)
        }
        notifyDataSetChanged()
    }

    private fun getEntry(position: Int): Tag {
        return values[position]
    }

    interface Listener {
        fun onTagClicked(entry: Tag)
    }
}
