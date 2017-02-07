package com.commit451.gitlab.viewHolder

import `in`.uncod.android.bypass.Bypass
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
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.BypassImageGetterFactory
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod
import com.squareup.picasso.Picasso
import com.vdurmont.emoji.EmojiParser

/**
 * Notes, aka comments
 */
class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): NoteViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(view)
        }
    }

    @BindView(R.id.title) lateinit var textTitle: TextView
    @BindView(R.id.summary) lateinit var textSummary: TextView
    @BindView(R.id.creation_date) lateinit var textCreationDate: TextView
    @BindView(R.id.icon) lateinit var imageAvatar: ImageView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(note: Note, bypass: Bypass, project: Project) {
        if (note.createdAt != null) {
            textCreationDate.text = DateUtil.getRelativeTimeSpanString(itemView.context, note.createdAt)
        }

        if (note.author != null) {
            textTitle.text = note.author.username
        }

        var summary = ""
        if (note.body != null) {
            summary = note.body
            summary = EmojiParser.parseToUnicode(summary)
        }

        val getter = BypassImageGetterFactory.create(textSummary,
                Picasso.with(itemView.context),
                App.get().getAccount().serverUrl.toString(),
                project)
        textSummary.text = bypass.markdownToSpannable(summary, getter)
        textSummary.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl)

        Picasso.with(itemView.context)
                .load(ImageUtil.getAvatarUrl(note.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(imageAvatar)
    }
}
