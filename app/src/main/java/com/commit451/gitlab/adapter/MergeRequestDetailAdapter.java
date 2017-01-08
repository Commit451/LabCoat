package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.MergeRequestHeaderViewHolder;
import com.commit451.gitlab.viewHolder.NoteViewHolder;

import java.util.LinkedList;
import java.util.List;

import in.uncod.android.bypass.Bypass;

/**
 * Shows the comments and details of a merge request
 */
public class MergeRequestDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_FOOTER = 2;

    private static final int HEADER_COUNT = 1;
    private static final int FOOTER_COUNT = 1;

    private LinkedList<Note> notes;
    private MergeRequest mergeRequest;
    private boolean loading = false;
    private Bypass bypass;
    private Project project;

    public MergeRequestDetailAdapter(Context context, MergeRequest mergeRequest, Project project) {
        this.mergeRequest = mergeRequest;
        notes = new LinkedList<>();
        bypass = new Bypass(context);
        this.project = project;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return MergeRequestHeaderViewHolder.inflate(parent);
        } else if (viewType == TYPE_COMMENT) {
            return NoteViewHolder.inflate(parent);
        } else if (viewType == TYPE_FOOTER) {
            return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalArgumentException("No view type matches");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MergeRequestHeaderViewHolder) {
            ((MergeRequestHeaderViewHolder) holder).bind(mergeRequest, bypass, project);
        } else if (holder instanceof NoteViewHolder) {
            Note note = getNoteAt(position);
            ((NoteViewHolder) holder).bind(note, bypass, project);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(loading);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size() + HEADER_COUNT + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else if (position == HEADER_COUNT + notes.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_COMMENT;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public Note getNoteAt(int position) {
        return notes.get(position - HEADER_COUNT);
    }

    public void addNote(Note note) {
        notes.addFirst(note);
        notifyItemInserted(HEADER_COUNT);
    }

    public void setNotes(List<Note> notes) {
        this.notes.clear();
        addNotes(notes);
    }

    public void addNotes(List<Note> notes) {
        if (!notes.isEmpty()) {
            this.notes.addAll(notes);
        }
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(notes.size() + HEADER_COUNT);
    }

    public static int getHeaderCount() {
        return HEADER_COUNT;
    }
}
