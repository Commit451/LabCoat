package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.viewHolders.MemberViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jawn on 7/28/2015.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberViewHolder> {

    public interface Listener {
        void onUserClicked(User user);
    }

    private Listener mListener;

    private ArrayList<User> mValues;

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onUserClicked(getValueAt(position));
        }
    };

    public User getValueAt(int position) {
        return mValues.get(position);
    }

    public MemberAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    public void setData(Collection<User> data) {
        mValues.clear();
        if (data != null) {
            mValues.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MemberViewHolder holder = MemberViewHolder.create(parent);
        holder.itemView.setOnClickListener(mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MemberViewHolder holder, int position) {
        User user = getValueAt(position);
        holder.bind(user);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addUser(User user) {
        mValues.add(0, user);
        notifyItemInserted(0);
    }

    public void removeUser(long userId) {
        for(User u : mValues) {
            if(u.getId() == userId) {
                int index = mValues.indexOf(u);
                mValues.remove(u);
                notifyItemRemoved(index);
                break;
            }
        }
    }
}
