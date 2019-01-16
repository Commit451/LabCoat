package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.viewHolder.BreadcrumbViewHolder
import java.util.*

/**
 * Shows the current file path
 */
class BreadcrumbAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<BreadcrumbViewHolder>() {
    private val values: MutableList<Breadcrumb>

    init {
        values = ArrayList<Breadcrumb>()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreadcrumbViewHolder {
        val holder = BreadcrumbViewHolder.inflate(parent)
        holder.itemView.setOnClickListener { v ->
            val position = v.getTag(R.id.list_position) as Int
            val breadcrumb = getValueAt(position)
            if (breadcrumb != null) {
                breadcrumb.listener.onClick()
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: BreadcrumbViewHolder, position: Int) {
        var title = ""
        val showArrow = position != values.size - 1

        val breadcrumb = getValueAt(position)
        if (breadcrumb != null) {
            title = breadcrumb.title
        }

        holder.bind(title, showArrow)
        holder.itemView.setTag(R.id.list_position, position)
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setData(breadcrumbs: Collection<Breadcrumb>?) {
        values.clear()
        if (breadcrumbs != null) {
            values.addAll(breadcrumbs)
            notifyItemRangeInserted(0, breadcrumbs.size)
        }
        notifyDataSetChanged()
    }

    fun getValueAt(position: Int): Breadcrumb? {
        if (position < 0 || position >= values.size) {
            return null
        }

        return values[position]
    }

    class Breadcrumb(val title: String, val listener: Listener)

    interface Listener {
        fun onClick()
    }
}
