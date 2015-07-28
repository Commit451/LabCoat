package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.viewHolders.UserViewHolder;

import java.util.List;

/**
 * Created by Jawn on 7/28/2015.
 */
public class NewUserAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<User> mValues;

    public User getValueAt(int position) {
        return mValues.get(position);
    }

    public NewUserAdapter(List<User> items) {
        mValues = items;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserViewHolder holder = UserViewHolder.create(parent);
        return holder;
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        User user = getValueAt(position);
        holder.bind(user);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addUser(User user) {
        mValues.add(user);
        notifyItemInserted(mValues.size() - 1);
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
