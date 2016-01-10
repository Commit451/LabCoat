package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.viewHolder.GroupViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * All the groups
 * Created by John on 10/8/15.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {

    public interface Listener {
        void onGroupClicked(Group group, GroupViewHolder groupViewHolder);
    }
    private Listener mListener;

    private ArrayList<Group> mValues;

    public GroupAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            GroupViewHolder holder = (GroupViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onGroupClicked(getEntry(position), holder);
        }
    };

    public void setGroups(Collection<Group> groups) {
        mValues.clear();
        addGroups(groups);
    }

    public void addGroups(Collection<Group> groups) {
        if (groups != null) {
            mValues.addAll(groups);
        }
        notifyDataSetChanged();
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        GroupViewHolder holder = GroupViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {
        holder.itemView.setTag(R.id.list_position, position);
        holder.itemView.setTag(R.id.list_view_holder, holder);
        holder.bind(getEntry(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private Group getEntry(int position) {
        return mValues.get(position);
    }
}
