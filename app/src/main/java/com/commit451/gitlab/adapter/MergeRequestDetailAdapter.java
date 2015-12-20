package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.model.MergeRequest;
import com.commit451.gitlab.model.MergeRequestComment;
import com.commit451.gitlab.viewHolder.MergeRequestCommentViewHolder;
import com.commit451.gitlab.viewHolder.MergeRequestHeaderViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the comments and details of a merge request
 * Created by John on 11/16/15.
 */
public class MergeRequestDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_COMMENT = 1;

    private static final int HEADER_COUNT = 1;

    private ArrayList<MergeRequestComment> mNotes;
    private MergeRequest mMergeRequest;

    public MergeRequestDetailAdapter(MergeRequest mergeRequest) {
        mMergeRequest = mergeRequest;
        mNotes = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return MergeRequestHeaderViewHolder.newInstance(parent);
        } else if (viewType == TYPE_COMMENT) {
            RecyclerView.ViewHolder holder = MergeRequestCommentViewHolder.newInstance(parent);
            return holder;
        }
        throw new IllegalArgumentException("No view type matches");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MergeRequestHeaderViewHolder) {
            ((MergeRequestHeaderViewHolder) holder).bind(mMergeRequest);
        } else if (holder instanceof MergeRequestCommentViewHolder) {
            MergeRequestComment note = getNoteAt(position);
            ((MergeRequestCommentViewHolder) holder).bind(note);
        }
    }

    @Override
    public int getItemCount() {
        return mNotes.size() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_COMMENT;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public MergeRequestComment getNoteAt(int position) {
        return mNotes.get(position-1);
    }

    public void addNote(MergeRequestComment note) {
        mNotes.add(note);
        notifyItemInserted(mNotes.size() + HEADER_COUNT);
    }

    public void addNotes(List<MergeRequestComment> notes) {
        if (!notes.isEmpty()) {
            mNotes.clear();
            mNotes.addAll(notes);
        }
        notifyDataSetChanged();
    }

    public void updateMergeRequest(MergeRequest mergeRequest) {
        mMergeRequest = mergeRequest;
        notifyItemChanged(0);
    }
}
