package com.commit451.gitlab.adapter

import `in`.uncod.android.bypass.Bypass
import `in`.uncod.android.bypass.ImageSpanClickListener
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.activity.FullscreenImageActivity
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.BypassFactory
import com.commit451.gitlab.viewHolder.IssueHeaderViewHolder
import com.commit451.gitlab.viewHolder.IssueLabelsViewHolder
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.NoteViewHolder
import java.util.*

/**
 * Nice notes
 */
class IssueDetailsAdapter(context: Context, private var issue: Issue?, private val project: Project) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private val TYPE_HEADER = 0
        private val TYPE_HEADER_LABEL = 1
        private val TYPE_COMMENT = 2
        private val TYPE_FOOTER = 3

        val headerCount = 2
        private val FOOTER_COUNT = 1
    }

    private val notes: LinkedList<Note> = LinkedList()
    private var loading = false
    private var imageClickListener = ImageSpanClickListener { view, imageSpan,
                                                              imageUrl ->
        val intent = FullscreenImageActivity.newIntent(view.context, project)
        intent.putExtra(FullscreenImageActivity.IMAGE_URL, imageUrl)
        context.startActivity(intent)
    }
    private val bypass: Bypass = BypassFactory.create(context, imageClickListener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            return IssueHeaderViewHolder.inflate(parent)
        } else if (viewType == TYPE_HEADER_LABEL) {
            return IssueLabelsViewHolder.inflate(parent)
        } else if (viewType == TYPE_COMMENT) {
            return NoteViewHolder.inflate(parent)
        } else if (viewType == TYPE_FOOTER) {
            return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalArgumentException("No view type matches")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IssueHeaderViewHolder) {
            holder.bind(issue!!, bypass, project)
        } else if (holder is IssueLabelsViewHolder) {
            holder.bind(issue!!.labels)
        } else if (holder is NoteViewHolder) {
            val note = getNoteAt(position)
            holder.bind(note, bypass, project)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        }
    }

    override fun getItemCount(): Int {
        return notes.size + headerCount + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_HEADER
        } else if (position == 1) {
            return TYPE_HEADER_LABEL
        } else if (position == headerCount + notes.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_COMMENT
        }
    }

    fun getNoteAt(position: Int): Note {
        return notes[position - headerCount]
    }

    fun setNotes(notes: List<Note>) {
        this.notes.clear()
        addNotes(notes)
    }

    fun addNotes(notes: List<Note>) {
        if (!notes.isEmpty()) {
            this.notes.addAll(notes)
            notifyItemRangeChanged(headerCount, headerCount + this.notes.size)
        }
    }

    fun addNote(note: Note) {
        notes.addFirst(note)
        notifyItemInserted(headerCount)
    }

    fun updateIssue(issue: Issue) {
        val oldLabels = this.issue!!.labels
        this.issue = issue
        notifyItemChanged(0)
        if (oldLabels.size != this.issue!!.labels.size) {
            notifyItemChanged(1)
        }
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(notes.size + headerCount)
    }
}
