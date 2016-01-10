package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.viewHolder.IssueHeaderViewHolder;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.NoteViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Nice notes
 * Created by Jawn on 8/6/2015.
 */
public class IssueDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_FOOTER = 2;

    private static final int HEADER_COUNT = 1;
    private static final int FOOTER_COUNT = 1;

    private ArrayList<Note> mNotes;
    private Issue mIssue;
    private boolean mLoading = false;

    public IssueDetailsAdapter(Issue issue) {
        mIssue = issue;
        mNotes = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return IssueHeaderViewHolder.inflate(parent);
        } else if (viewType == TYPE_COMMENT) {
            RecyclerView.ViewHolder holder = NoteViewHolder.inflate(parent);
            return holder;
        } else if (viewType == TYPE_FOOTER) {
            return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalArgumentException("No view type matches");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof IssueHeaderViewHolder) {
            ((IssueHeaderViewHolder) holder).bind(mIssue);
        } else if (holder instanceof NoteViewHolder) {
            Note note = getNoteAt(position);
            ((NoteViewHolder) holder).bind(note);
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
        return mNotes.get(position-1);
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

    public void addNote(Note note) {
        mNotes.add(note);
        notifyItemInserted(HEADER_COUNT);
    }

    public void updateIssue(Issue issue) {
        mIssue = issue;
        notifyItemChanged(0);
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyItemChanged(mNotes.size() + HEADER_COUNT);
    }

    public static int getHeaderCount() {
        return HEADER_COUNT;
    }
}
