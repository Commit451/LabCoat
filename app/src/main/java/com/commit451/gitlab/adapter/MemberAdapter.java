package com.commit451.gitlab.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.ProjectNamespace;
import com.commit451.gitlab.viewHolder.ProjectMemberFooterViewHolder;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows a projects members and a groups members
 * Created by Jawn on 7/28/2015.
 */
public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MEMBER = 0;
    private static final int TYPE_FOOTER = 1;

    private static final int FOOTER_COUNT = 1;

    public interface Listener {
        void onProjectMemberClicked(Member member, ProjectMemberViewHolder memberGroupViewHolder);
        void onRemoveMember(Member member);
        void onChangeAccess(Member member);
        void onSeeGroupClicked();
    }

    private Listener mListener;

    private ArrayList<Member> mProjectMembers;
    private ProjectNamespace mNamespace;

    private final View.OnClickListener mProjectMemberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            ProjectMemberViewHolder memberGroupViewHolder = (ProjectMemberViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onProjectMemberClicked(getProjectMember(position), memberGroupViewHolder);
        }
    };

    private final View.OnClickListener mFooterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onSeeGroupClicked();
        }
    };

    private final GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            int viewType = getItemViewType(position);
            if (viewType == TYPE_FOOTER) {
                return 2;
            } else {
                return 1;
            }
        }
    };

    public Member getProjectMember(int position) {
        return mProjectMembers.get(position);
    }

    public MemberAdapter(Listener listener) {
        mListener = listener;
        mProjectMembers = new ArrayList<>();
    }

    public void setProjectMembers(Collection<Member> data) {
        mProjectMembers.clear();
        addProjectMembers(data);
    }

    public void addProjectMembers(Collection<Member> data) {
        if (data != null) {
            mProjectMembers.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setNamespace(ProjectNamespace namespace) {
        mNamespace = namespace;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MEMBER:
                ProjectMemberViewHolder projectViewHolder = ProjectMemberViewHolder.inflate(parent);
                projectViewHolder.itemView.setOnClickListener(mProjectMemberClickListener);
                return projectViewHolder;
            case TYPE_FOOTER:
                ProjectMemberFooterViewHolder footerHolder = ProjectMemberFooterViewHolder.inflate(parent);
                footerHolder.itemView.setOnClickListener(mFooterClickListener);
                return footerHolder;
        }
        throw new IllegalStateException("No idea what to inflate with view type of " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProjectMemberFooterViewHolder) {
            if (mNamespace == null) {
                holder.itemView.setVisibility(View.GONE);
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                ((ProjectMemberFooterViewHolder) holder).bind(mNamespace);
            }
        } else if (holder instanceof ProjectMemberViewHolder) {
            final Member member = getProjectMember(position);
            ((ProjectMemberViewHolder) holder).bind(member);
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
            ((ProjectMemberViewHolder) holder).mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_change_access:
                            mListener.onChangeAccess(member);
                            return true;
                        case R.id.action_remove:
                            mListener.onRemoveMember(member);
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mProjectMembers.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mProjectMembers.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_MEMBER;
        }
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return mSpanSizeLookup;
    }

    public void addMember(Member member) {
        mProjectMembers.add(0, member);
        notifyItemInserted(0);
    }

    public void removeMember(Member member) {
        int position = mProjectMembers.indexOf(member);
        mProjectMembers.remove(member);
        notifyItemRemoved(position);
    }
}
