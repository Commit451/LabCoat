package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.viewHolder.AssigneeViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Shows the possible assignees
 * Created by Jawn on 12/18/2015.
 */
public class AssigneeAdapter extends RecyclerView.Adapter<AssigneeViewHolder> {

    private static final int HEADER_COUNT = 1;

    private int mColorControlHighlight;
    private ArrayList<User> mUsers;
    private User mSelectedAssignee;

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            if (position > 0) {
                mSelectedAssignee = getUser(position);
            } else {
                mSelectedAssignee = null;
            }
            notifyDataSetChanged();
        }
    };

    public AssigneeAdapter(Context context, User currentAssignee) {
        mUsers = new ArrayList<>();
        mSelectedAssignee = currentAssignee;
        mColorControlHighlight = Easel.getThemeAttrColor(context, R.attr.colorControlHighlight);
    }

    public void setUsers(Collection<User> users) {
        mUsers.clear();
        if (users != null) {
            mUsers.addAll(users);
        }
        notifyDataSetChanged();
    }

    @Override
    public AssigneeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AssigneeViewHolder holder = AssigneeViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(AssigneeViewHolder holder, int position) {
        if (position == 0) {
            holder.bind(null, mColorControlHighlight, mSelectedAssignee == null);
        } else {
            User user = getUser(position);
            holder.bind(user, mColorControlHighlight, mSelectedAssignee == null ? false : mSelectedAssignee.equals(user));
        }
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mUsers.size() + HEADER_COUNT;
    }

    private User getUser(int position) {
        return mUsers.get(position - HEADER_COUNT);
    }
}
