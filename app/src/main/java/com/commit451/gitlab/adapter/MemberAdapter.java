package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.viewHolders.MemberGroupHeaderViewHolder;
import com.commit451.gitlab.viewHolders.MemberGroupViewHolder;
import com.commit451.gitlab.viewHolders.MemberProjectHeaderViewHolder;
import com.commit451.gitlab.viewHolders.MemberProjectViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a projects members and a groups members
 * Created by Jawn on 7/28/2015.
 */
public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROJECT_HEADER = 0;
    private static final int TYPE_PROJECT_MEMBER = 1;
    private static final int TYPE_GROUP_HEADER = 2;
    private static final int TYPE_GROUP_MEMBER = 3;

    private static final int HEADER_COUNT = 2;

    public interface Listener {
        void onProjectMemberClicked(User user, MemberProjectViewHolder memberGroupViewHolder);
        void onGroupMemberClicked(User user, MemberGroupViewHolder memberGroupViewHolder);
    }

    private Listener mListener;

    private Project mProject;
    private ArrayList<User> mProjectMembers;
    private ArrayList<User> mGroupMembers;

    private final View.OnClickListener mProjectMemberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            MemberProjectViewHolder memberGroupViewHolder = (MemberProjectViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onProjectMemberClicked(getProjectMember(position), memberGroupViewHolder);
        }
    };

    private final View.OnClickListener mGroupMemberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            MemberGroupViewHolder memberGroupViewHolder = (MemberGroupViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onGroupMemberClicked(getGroupMember(position), memberGroupViewHolder);
        }
    };

    public User getProjectMember(int position) {
        return mProjectMembers.get(position - 1);
    }

    public User getGroupMember(int position) {
        return mGroupMembers.get(position - mProjectMembers.size() - HEADER_COUNT);
    }

    public MemberAdapter(Listener listener) {
        mListener = listener;
        mProjectMembers = new ArrayList<>();
        mGroupMembers = new ArrayList<>();
    }

    public void setProjectMembers(Collection<User> data) {
        mProjectMembers.clear();
        if (data != null) {
            mProjectMembers.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setGroupMembers(Collection<User> members) {
        mGroupMembers.clear();
        if (members != null) {
            mGroupMembers.addAll(members);
        }
        notifyDataSetChanged();
    }

    public void setProject(Project project) {
        mProject = project;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PROJECT_HEADER:
                return MemberProjectHeaderViewHolder.newInstance(parent);
            case TYPE_PROJECT_MEMBER:
                MemberProjectViewHolder projectViewHolder = MemberProjectViewHolder.newInstance(parent);
                projectViewHolder.itemView.setOnClickListener(mProjectMemberClickListener);
                return projectViewHolder;
            case TYPE_GROUP_HEADER:
                return MemberGroupHeaderViewHolder.newInstance(parent);
            case TYPE_GROUP_MEMBER:
                MemberGroupViewHolder groupViewHolder = MemberGroupViewHolder.newInstance(parent);
                groupViewHolder.itemView.setOnClickListener(mGroupMemberClickListener);
                return groupViewHolder;
        }
        throw new IllegalStateException("No idea what to inflate with view type of " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MemberProjectHeaderViewHolder) {
            if (mProjectMembers.isEmpty()) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof MemberProjectViewHolder) {
            User user = getProjectMember(position);
            ((MemberProjectViewHolder) holder).bind(user);
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
        } else if (holder instanceof MemberGroupHeaderViewHolder) {
            if (mGroupMembers.isEmpty()) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                ((MemberGroupHeaderViewHolder) holder).bind(mProject.getNamespace());
            }
        } else if (holder instanceof MemberGroupViewHolder) {
            User user = getGroupMember(position);
            ((MemberGroupViewHolder) holder).bind(user);
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
        }
    }

    @Override
    public int getItemCount() {
        return mProjectMembers.size() + mGroupMembers.size() + HEADER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PROJECT_HEADER;
        } else if (position == mProjectMembers.size() + 1) {
            return TYPE_GROUP_HEADER;
        } else if (position < mProjectMembers.size() + 1) {
            return TYPE_PROJECT_MEMBER;
        }  else if (position > mProjectMembers.size() + 1) {
            return TYPE_GROUP_MEMBER;
        } else {
            throw new IllegalStateException("No type for position " + position);
        }
    }

    public void addUser(User user) {
        mProjectMembers.add(0, user);
        notifyItemInserted(0);
    }

    public void removeUser(long userId) {
        for(User u : mProjectMembers) {
            if(u.getId() == userId) {
                int index = mProjectMembers.indexOf(u);
                mProjectMembers.remove(u);
                notifyItemRemoved(index);
                break;
            }
        }
    }
}
