package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.viewHolder.FeedEntryViewHolder
import java.util.*

/**
 * Adapts the feeds
 */
class FeedAdapter(internal var listener: FeedAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<FeedEntryViewHolder>() {

    var values: ArrayList<Entry> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedEntryViewHolder {
        val holder = FeedEntryViewHolder.inflate(parent)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            listener.onFeedEntryClicked(getEntry(position))
        }
        return holder
    }

    override fun onBindViewHolder(holder: FeedEntryViewHolder, position: Int) {
        holder.itemView.setTag(R.id.list_position, position)
        holder.bind(getEntry(position))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setEntries(entries: Collection<Entry>?) {
        values.clear()
        if (entries != null) {
            values.addAll(entries)
        }
        notifyDataSetChanged()
    }

    private fun getEntry(position: Int): Entry {
        return values[position]
    }

    interface Listener {
        fun onFeedEntryClicked(entry: Entry)
    }
}
