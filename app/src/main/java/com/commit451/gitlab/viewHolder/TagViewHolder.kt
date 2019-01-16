package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.recyclerview.context
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Tag

/**
 * Label
 */
class TagViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): TagViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_tag, parent, false)
            return TagViewHolder(view)
        }
    }

    @BindView(R.id.title)
    lateinit var title: TextView

    var colorHighlighted: Int = 0

    init {
        ButterKnife.bind(this, view)
        colorHighlighted = context.themeAttrColor(R.attr.colorControlHighlight)
    }

    fun bind(tag: Tag, selected: Boolean) {
        title.text = tag.name
        if (selected) {
            itemView.setBackgroundColor(colorHighlighted)
        } else {
            itemView.background = null
        }
    }
}