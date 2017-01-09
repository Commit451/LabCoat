package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
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

    @BindView(R.id.title) lateinit var textTitle: TextView
    @BindView(R.id.file_name) lateinit var textFileName: TextView

    init {
        ButterKnife.bind(this, view)
    }

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