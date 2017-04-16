package com.commit451.gitlab.adapter

import `in`.uncod.android.bypass.Bypass
import `in`.uncod.android.bypass.ImageSpanClickListener
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.activity.FullscreenImageActivity
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.BypassFactory
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import com.commit451.gitlab.viewHolder.MergeRequestHeaderViewHolder
import com.commit451.gitlab.viewHolder.NoteViewHolder
import java.util.*

/**
 * Shows the comments and details of a merge request
 */
class MergeRequestDetailAdapter(context: Context, private val mergeRequest: MergeRequest, private val project: Project) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        val TYPE_HEADER = 0
        val TYPE_COMMENT = 1
        val TYPE_FOOTER = 2

        val headerCount = 1
        val FOOTER_COUNT = 1
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
            return MergeRequestHeaderViewHolder.inflate(parent)
        } else if (viewType == TYPE_COMMENT) {
            return NoteViewHolder.inflate(parent)
        } else if (viewType == TYPE_FOOTER) {
            return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalArgumentException("No view type matches")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MergeRequestHeaderViewHolder) {
            holder.bind(mergeRequest, bypass, project)
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
        if (isPositionHeader(position)) {
            return TYPE_HEADER
        } else if (position == headerCount + notes.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_COMMENT
        }
    }

    fun getNoteAt(position: Int): Note {
        return notes[position - headerCount]
    }

    fun addNote(note: Note) {
        notes.addFirst(note)
        notifyItemInserted(headerCount)
    }

    fun setNotes(notes: List<Note>) {
        this.notes.clear()
        addNotes(notes)
    }

    fun addNotes(notes: List<Note>) {
        if (!notes.isEmpty()) {
            this.notes.addAll(notes)
        }
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(notes.size + headerCount)
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }
}
