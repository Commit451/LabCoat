package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.PopupMenu
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.RepositoryTreeObject
import com.commit451.gitlab.viewHolder.FileViewHolder
import java.util.*

/**
 * Shows the files
 */
class FileAdapter(private val listener: FileAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<FileViewHolder>() {

    val values: ArrayList<RepositoryTreeObject> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val holder = FileViewHolder.inflate(parent)
        holder.itemView.setOnClickListener { v ->
            val position = v.getTag(R.id.list_position) as Int
            val treeItem = getValueAt(position)

            if (treeItem.type == RepositoryTreeObject.TYPE_FOLDER) {
                listener.onFolderClicked(treeItem)
            } else if (treeItem.type == RepositoryTreeObject.TYPE_FILE) {
                listener.onFileClicked(treeItem)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val treeItem = getValueAt(position)
        holder.bind(treeItem)
        holder.itemView.setTag(R.id.list_position, position)
        holder.popupMenu.menu.findItem(R.id.action_open_external).setVisible(treeItem.type == RepositoryTreeObject.TYPE_FILE)
        holder.popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_copy -> {
                    listener.onCopyClicked(treeItem)
                    return@OnMenuItemClickListener true
                }
                R.id.action_share -> {
                    listener.onShareClicked(treeItem)
                    return@OnMenuItemClickListener true
                }
                R.id.action_open -> {
                    listener.onOpenInBrowserClicked(treeItem)
                    return@OnMenuItemClickListener true
                }
                R.id.action_open_external -> {
                    listener.onOpenExternalClicked(treeItem)
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setData(values: Collection<RepositoryTreeObject>?) {
        this.values.clear()
        if (values != null) {
            this.values.addAll(values)
        }
        notifyDataSetChanged()
    }

    fun clear() {
        values.clear()
        notifyDataSetChanged()
    }

    fun getValueAt(position: Int): RepositoryTreeObject {
        return values[position]
    }

    interface Listener {
        fun onFolderClicked(treeItem: RepositoryTreeObject)
        fun onFileClicked(treeItem: RepositoryTreeObject)
        fun onCopyClicked(treeItem: RepositoryTreeObject)
        fun onShareClicked(treeItem: RepositoryTreeObject)
        fun onOpenInBrowserClicked(treeItem: RepositoryTreeObject)
        fun onOpenExternalClicked(treeItem: RepositoryTreeObject)
    }
}
