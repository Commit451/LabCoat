package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Todo
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.TodoViewHolder
import java.util.*

/**
 * Issues adapter
 */
class TodoAdapter(private val listener: TodoAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {

        val FOOTER_COUNT = 1

        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1
    }

    private val values: ArrayList<Todo> = ArrayList()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = TodoViewHolder.inflate(parent)
                holder.itemView.setOnClickListener { v ->
                    val position = v.getTag(R.id.list_position) as Int
                    listener.onTodoClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No holder for view type " + viewType)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder is TodoViewHolder) {
            val todo = getValueAt(position)
            holder.bind(todo)
            holder.itemView.setTag(R.id.list_position, position)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        } else {
            throw IllegalStateException("What is this holder?")
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

    fun setData(todos: Collection<Todo>?) {
        values.clear()
        addData(todos)
    }

    fun addData(todos: Collection<Todo>?) {
        if (todos != null) {
            values.addAll(todos)
        }
        notifyDataSetChanged()
    }

    fun getValueAt(position: Int): Todo {
        return values[position]
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onTodoClicked(todo: Todo)
    }
}
