package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.viewHolder.IssueHeaderViewHolder;
import com.commit451.gitlab.viewHolder.IssueLabelsViewHolder;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.NoteViewHolder;

import java.util.LinkedList;
import java.util.List;

import in.uncod.android.bypass.Bypass;

/**
 * Nice notes
 */
public class IssueDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_HEADER_LABEL = 1;
    private static final int TYPE_COMMENT = 2;
    private static final int TYPE_FOOTER = 3;

    private static final int HEADER_COUNT = 2;
    private static final int FOOTER_COUNT = 1;

    private Project mProject;
    private LinkedList<Note> mNotes;
    private Issue mIssue;
    private boolean mLoading = false;
    private Bypass mBypass;

    public IssueDetailsAdapter(Context context, Issue issue, Project project) {
        mIssue = issue;
        mNotes = new LinkedList<>();
        mBypass = new Bypass(context);
        mProject = project;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return IssueHeaderViewHolder.inflate(parent);
        } else if (viewType == TYPE_HEADER_LABEL) {
            return IssueLabelsViewHolder.inflate(parent);
        } else if (viewType == TYPE_COMMENT) {
            return NoteViewHolder.inflate(parent);
        } else if (viewType == TYPE_FOOTER) {
            return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalArgumentException("No view type matches");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof IssueHeaderViewHolder) {
            ((IssueHeaderViewHolder) holder).bind(mIssue);
        } else if (holder instanceof IssueLabelsViewHolder) {
            ((IssueLabelsViewHolder) holder).bind(mIssue.getLabels());
        } else if (holder instanceof NoteViewHolder) {
            Note note = getNoteAt(position);
            ((NoteViewHolder) holder).bind(note, mBypass, mProject);
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
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == 1) {
            return TYPE_HEADER_LABEL;
        } else if (position == HEADER_COUNT + mNotes.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_COMMENT;
        }
    }

    public Note getNoteAt(int position) {
        return mNotes.get(position - HEADER_COUNT);
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
        mNotes.addFirst(note);
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
