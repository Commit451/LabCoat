package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alorma.diff.lib.DiffTextView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.fileName
import com.commit451.gitlab.model.api.Diff

/**
 * Displays a diff to a user
 */
class DiffViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): DiffViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_diff, parent, false)
            return DiffViewHolder(view)
        }
    }

    private val textFileTitle: TextView by bindView(R.id.file_title)
    private val textDiff: DiffTextView by bindView(R.id.diff)

    fun bind(diff: Diff) {
        textFileTitle.text = diff.fileName
        textDiff.text = diff.diff
    }
}
