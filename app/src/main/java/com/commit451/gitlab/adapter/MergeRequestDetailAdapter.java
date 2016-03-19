package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.MergeRequestHeaderViewHolder;
import com.commit451.gitlab.viewHolder.NoteViewHolder;

import java.util.LinkedList;
import java.util.List;

import in.uncod.android.bypass.Bypass;

/**
 * Shows the comments and details of a merge request
 * Created by John on 11/16/15.
 */
public class MergeRequestDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_FOOTER = 2;

    private static final int HEADER_COUNT = 1;
    private static final int FOOTER_COUNT = 1;

    private LinkedList<Note> mNotes;
    private MergeRequest mMergeRequest;
    private boolean mLoading = false;
    private Bypass mBypass;

    public MergeRequestDetailAdapter(Context context, MergeRequest mergeRequest) {
        mMergeRequest = mergeRequest;
        mNotes = new LinkedList<>();
        mBypass = new Bypass(context);
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
            ((MergeRequestHeaderViewHolder) holder).bind(mMergeRequest, mBypass);
        } else if (holder instanceof NoteViewHolder) {
            Note note = getNoteAt(position);
            ((NoteViewHolder) holder).bind(note, mBypass);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(mLoading);
        }
    }

    @Override
    public int getItemCount() {
        return mNotes.size() + HEADER_COUNT + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else if (position == HEADER_COUNT + mNotes.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_COMMENT;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public Note getNoteAt(int position) {
        return mNotes.get(position - HEADER_COUNT);
    }

    public void addNote(Note note) {
        mNotes.addFirst(note);
        notifyItemInserted(HEADER_COUNT);
    }

    public void setNotes(List<Note> notes) {
        mNotes.clear();
        addNotes(notes);
    }

    public void addNotes(List<Note> notes) {
        if (!notes.isEmpty()) {
            mNotes.addAll(notes);
        }
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyItemChanged(mNotes.size() + HEADER_COUNT);
    }

    public static int getHeaderCount() {
        return HEADER_COUNT;
    }
}
