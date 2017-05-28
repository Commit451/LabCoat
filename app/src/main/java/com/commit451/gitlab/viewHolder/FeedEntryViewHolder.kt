package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.formatAsHtml
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.transformation.CircleTransformation

/**
 * Represents the view of an item in the RSS feed
 */
class FeedEntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): FeedEntryViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_entry, parent, false)
            return FeedEntryViewHolder(view)
        }
    }

    @BindView(R.id.image) lateinit var image: ImageView
    @BindView(R.id.title) lateinit var textTitle: TextView
    @BindView(R.id.description) lateinit var textSummary: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(entry: Entry) {
        App.get().picasso
                .load(entry.thumbnail.url)
                .transform(CircleTransformation())
                .into(image)

        textTitle.text = entry.title.formatAsHtml()
        textSummary.text = entry.summary.formatAsHtml()
    }
}
