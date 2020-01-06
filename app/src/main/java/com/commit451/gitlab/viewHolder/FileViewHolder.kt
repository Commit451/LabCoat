package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.getDrawableForType
import com.commit451.gitlab.model.api.RepositoryTreeObject

/**
 * Files, yay!
 */
class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): FileViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_file, parent, false)
            return FileViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.file_title)
    private val image: ImageView by bindView(R.id.file_image)
    private val buttonMore: ImageView by bindView(R.id.file_more)

    val popupMenu: PopupMenu

    init {
        popupMenu = PopupMenu(itemView.context, buttonMore)
        popupMenu.menuInflater.inflate(R.menu.item_menu_file, popupMenu.menu)

        buttonMore.setOnClickListener { popupMenu.show() }
    }

    fun bind(treeItem: RepositoryTreeObject) {
        textTitle.text = treeItem.name
        image.setImageResource(treeItem.getDrawableForType())
    }
}
