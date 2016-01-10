package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.viewHolder.IssueHeaderViewHolder;
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

    private static final int HEADER_COUNT = 1;

    private ArrayList<Note> mNotes;
    private ArrayList<UserBasic> mUsers;
    private ArrayList<Milestone> mMilestones;
    private Issue mIssue;

    public IssueDetailsAdapter(Issue issue) {
        mIssue = issue;
        mNotes = new ArrayList<>();
        mUsers = new ArrayList<>();
        mMilestones = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return IssueHeaderViewHolder.inflate(parent);
        } else if (viewType == TYPE_COMMENT) {
            RecyclerView.ViewHolder holder = NoteViewHolder.inflate(parent);
            return holder;
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

    public Note getNoteAt(int position) {
        return mNotes.get(position-1);
    }

    public void addNotes(List<Note> notes) {
        if (!notes.isEmpty()) {
            mNotes.clear();
            mNotes.addAll(notes);
        }
        notifyDataSetChanged();
    }

    public void addNote(Note note) {
        mNotes.add(note);
        notifyItemInserted(mNotes.size() + HEADER_COUNT);
    }

    public void addUsers(List<UserBasic> users) {
        if (!users.isEmpty()) {
            users.clear();
            mUsers.addAll(users);
        }
        notifyDataSetChanged();
    }

    public void addMilestones(List<Milestone> milestones) {
        if (!milestones.isEmpty()) {
            milestones.clear();
            mMilestones.addAll(milestones);
        }
        notifyDataSetChanged();
    }

    public void updateIssue(Issue issue) {
        mIssue = issue;
        notifyItemChanged(0);
    }
}
