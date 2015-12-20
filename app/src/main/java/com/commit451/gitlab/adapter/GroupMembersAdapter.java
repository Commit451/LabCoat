package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.viewHolders.MemberProjectViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter for a list of users
 * Created by John on 9/28/15.
 */
public class GroupMembersAdapter extends RecyclerView.Adapter<MemberProjectViewHolder>  {

    public interface Listener {
        void onUserClicked(User user, MemberProjectViewHolder userViewHolder);
        void onUserRemoveClicked(User user);
        void onUserChangeAccessClicked(User user);
    }
    private Listener mListener;
    private ArrayList<User> mData;

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            MemberProjectViewHolder holder = (MemberProjectViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onUserClicked(getUser(position), holder);
        }
    };

    public GroupMembersAdapter(Listener listener) {
        mListener = listener;
        mData = new ArrayList<>();
    }

    @Override
    public MemberProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MemberProjectViewHolder holder = MemberProjectViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(MemberProjectViewHolder holder, int position) {
        final User user = mData.get(position);
        holder.bind(user);
        holder.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_change_access:
                        mListener.onUserChangeAccessClicked(user);
                        return true;
                    case R.id.action_remove:
                        mListener.onUserRemoveClicked(user);
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

    private User getUser(int position) {
        return mData.get(position);
    }

    public void setData(Collection<User> users) {
        mData.clear();
        if (users != null) {
            mData.addAll(users);
        }
        notifyDataSetChanged();
    }

    public void addUser(User user) {
        mData.add(user);
        notifyItemInserted(0);
    }

    public void removeUser(User user) {
        int index = mData.indexOf(user);
        mData.remove(index);
        notifyItemRemoved(index);
    }
}
