package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.transformation.CircleTransformation
import com.squareup.picasso.Picasso

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
        Picasso.with(itemView.context)
                .load(entry.thumbnail.url)
                .transform(CircleTransformation())
                .into(image)

        textTitle.text = Html.fromHtml(entry.title)
        textSummary.text = Html.fromHtml(entry.summary)
    }
}
