package com.commit451.gitlab.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder

/**
 * An adapter that supports showing loading.
 */
class BaseAdapter<T, VH: RecyclerView.ViewHolder>(
        private val onCreateViewHolder: (parent: ViewGroup, viewType: Int) -> RecyclerView.ViewHolder,
        private val onBindViewHolder: (viewHolder: VH, position: Int, item: T) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val FOOTER_COUNT = 1

        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = Int.MAX_VALUE
    }

    val items = mutableListOf<T>()

    private var isLoading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                return this.onCreateViewHolder.invoke(parent, viewType)
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No known ViewHolder for type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LoadingFooterViewHolder) {
            holder.bind(isLoading)
        } else {
            this.onBindViewHolder.invoke(holder as VH, position, items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    fun set(collection: Collection<T>?) {
        items.clear()
        collection?.let {
            items.addAll(it)
        }
        notifyDataSetChanged()
    }

    fun add(item: T, index: Int = items.size) {
        items.add(index, item)
        notifyItemInserted(index)
    }

    fun addAll(collection: Collection<T>, index: Int = items.size) {
        items.addAll(index, collection)
        //for some reason (probably because of the loading indicator) this is broken
        //notifyItemRangeInserted(index, items.size)
        notifyDataSetChanged()
    }

    fun remove(item: T) {
        val index = items.indexOfFirst { it == item }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun removeAll(itemsToRemove: Collection<T>) {
        for (item in itemsToRemove) {
            remove(item)
        }
    }

    fun update(item: T) {
        val index = items.indexOfFirst { it == item }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun updateAll(items: List<T>) {
        for (item in items) {
            update(item)
        }
    }

    fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun setLoading(loading: Boolean) {
        this.isLoading = loading
        notifyItemChanged(items.size)
    }
}
