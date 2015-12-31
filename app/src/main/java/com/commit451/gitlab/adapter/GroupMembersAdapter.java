package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter for a list of users
 * Created by John on 9/28/15.
 */
public class GroupMembersAdapter extends RecyclerView.Adapter<ProjectMemberViewHolder>  {

    public interface Listener {
        void onUserClicked(Member member, ProjectMemberViewHolder userViewHolder);
        void onUserRemoveClicked(Member member);
        void onUserChangeAccessClicked(Member member);
    }
    private Listener mListener;
    private ArrayList<Member> mData;

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            ProjectMemberViewHolder holder = (ProjectMemberViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onUserClicked(getMember(position), holder);
        }
    };

    public GroupMembersAdapter(Listener listener) {
        mListener = listener;
        mData = new ArrayList<>();
    }

    @Override
    public ProjectMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ProjectMemberViewHolder holder = ProjectMemberViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ProjectMemberViewHolder holder, int position) {
        final Member member = mData.get(position);
        holder.bind(member);
        holder.mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_change_access:
                        mListener.onUserChangeAccessClicked(member);
                        return true;
                    case R.id.action_remove:
                        mListener.onUserRemoveClicked(member);
                        return true;
                }
                return false;
            }
        });
        holder.itemView.setTag(R.id.list_position, position);
        holder.itemView.setTag(R.id.list_view_holder, holder);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private Member getMember(int position) {
        return mData.get(position);
    }

    public void setData(Collection<Member> users) {
        mData.clear();
        if (users != null) {
            mData.addAll(users);
        }
        notifyDataSetChanged();
    }

    public void addMember(Member member) {
        mData.add(0, member);
        notifyItemInserted(0);
    }

    public void removeMember(Member member) {
        int index = mData.indexOf(member);
        mData.remove(index);
        notifyItemRemoved(index);
    }
}
