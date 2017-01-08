package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter for a list of users
 */
public class GroupMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int TYPE_MEMBER = 0;
    private static final int TYPE_FOOTER = 1;

    private static final int FOOTER_COUNT = 1;

    private Listener listener;
    private ArrayList<Member> values;
    private boolean loading = false;

    public GroupMembersAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MEMBER:
                ProjectMemberViewHolder holder = ProjectMemberViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag(R.id.list_position);
                        ProjectMemberViewHolder holder = (ProjectMemberViewHolder) v.getTag(R.id.list_view_holder);
                        listener.onUserClicked(getMember(position), holder);
                    }
                });
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No known ViewHolder for type " + viewType);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProjectMemberViewHolder) {
            final Member member = values.get(position);
            ((ProjectMemberViewHolder) holder).bind(member);
            ((ProjectMemberViewHolder) holder).popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_change_access:
                            listener.onUserChangeAccessClicked(member);
                            return true;
                        case R.id.action_remove:
                            listener.onUserRemoveClicked(member);
                            return true;
                    }
                    return false;
                }
            });
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
        } else if(holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(loading);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == values.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_MEMBER;
        }
    }

    @Override
    public int getItemCount() {
        return values.size() + FOOTER_COUNT;
    }

    private Member getMember(int position) {
        return values.get(position);
    }

    public void setData(Collection<Member> members) {
        values.clear();
        addData(members);
    }

    public void addData(Collection<Member> members) {
        if (members != null) {
            values.addAll(members);
        }
        notifyDataSetChanged();
    }

    public void addMember(Member member) {
        values.add(0, member);
        notifyItemInserted(0);
    }

    public void removeMember(Member member) {
        int index = values.indexOf(member);
        values.remove(index);
        notifyItemRemoved(index);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(values.size());
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isFooter(int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_FOOTER) {
            return true;
        }
        return false;
    }

    public interface Listener {
        void onUserClicked(Member member, ProjectMemberViewHolder userViewHolder);
        void onUserRemoveClicked(Member member);
        void onUserChangeAccessClicked(Member member);
    }
}
