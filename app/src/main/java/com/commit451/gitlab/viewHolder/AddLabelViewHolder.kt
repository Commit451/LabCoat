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
 * Shows the label on a screen where you can add labels
 */
class AddLabelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): AddLabelViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_label, parent, false)
            return AddLabelViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.title)

    fun bind(label: Label) {
        textTitle.text = label.name
        textTitle.setTextColor(label.getTitleColor())
        textTitle.setBackgroundColor(label.getColor())
    }
}
