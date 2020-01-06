package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.getColor
import com.commit451.gitlab.extension.getTitleColor
import com.commit451.gitlab.model.api.Label

/**
 * Label
 */
class LabelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): LabelViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_label, parent, false)
            return LabelViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.title)
    private val viewColor: View by bindView(R.id.color)

    fun bind(label: Label) {
        textTitle.text = label.name
        textTitle.setTextColor(label.getTitleColor())
        viewColor.setBackgroundColor(label.getColor())
    }
}
