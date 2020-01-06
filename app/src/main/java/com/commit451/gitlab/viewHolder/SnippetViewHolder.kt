package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Snippet

/**
 * Snippet
 */
class SnippetViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): SnippetViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_snippet, parent, false)
            return SnippetViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.title)
    private val textFileName: TextView by bindView(R.id.file_name)

    fun bind(snippet: Snippet) {
        textTitle.text = snippet.title
        if (snippet.fileName != null) {
            textFileName.visibility = View.VISIBLE
            textFileName.text = snippet.fileName
        } else {
            textFileName.visibility = View.GONE
        }
    }
}
