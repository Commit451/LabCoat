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

    private Project project;
    private LinkedList<Note> notes;
    private Issue issue;
    private boolean loading = false;
    private Bypass bypass;

    public IssueDetailsAdapter(Context context, Issue issue, Project project) {
        this.issue = issue;
        notes = new LinkedList<>();
        bypass = new Bypass(context);
        this.project = project;
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
            ((IssueHeaderViewHolder) holder).bind(issue, project);
        } else if (holder instanceof IssueLabelsViewHolder) {
            ((IssueLabelsViewHolder) holder).bind(issue.getLabels());
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
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == 1) {
            return TYPE_HEADER_LABEL;
        } else if (position == HEADER_COUNT + notes.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_COMMENT;
        }
    }

    public Note getNoteAt(int position) {
        return notes.get(position - HEADER_COUNT);
    }

    public void setNotes(List<Note> notes) {
        this.notes.clear();
        addNotes(notes);
    }

    public void addNotes(List<Note> notes) {
        if (!notes.isEmpty()) {
            this.notes.addAll(notes);
            notifyItemRangeChanged(HEADER_COUNT, HEADER_COUNT + this.notes.size());
        }
    }

    public void addNote(Note note) {
        notes.addFirst(note);
        notifyItemInserted(HEADER_COUNT);
    }

    public void updateIssue(Issue issue) {
        List<String> oldLabels = this.issue.getLabels();
        this.issue = issue;
        notifyItemChanged(0);
        if (oldLabels.size() != this.issue.getLabels().size()) {
            notifyItemChanged(1);
        }
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(notes.size() + HEADER_COUNT);
    }

    public static int getHeaderCount() {
        return HEADER_COUNT;
    }
}
