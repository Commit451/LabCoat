package com.commit451.gitlab.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.UserViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter for a list of users
 */
public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private Listener listener;
    private ArrayList<UserBasic> values;
    private boolean loading;

    private final GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
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

    public UserAdapter(Listener listener) {
        this.listener = listener;
        values = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                UserViewHolder holder = UserViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag(R.id.list_position);
                        UserViewHolder holder = (UserViewHolder) v.getTag(R.id.list_view_holder);
                        listener.onUserClicked(getUser(position), holder);
                    }
                });
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No known viewholder for type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(values.get(position));
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(loading);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == values.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return values.size() + FOOTER_COUNT;
    }

    private UserBasic getUser(int position) {
        return values.get(position);
    }

    public void setData(Collection<UserBasic> users) {
        values.clear();
        addData(users);
    }

    public void addData(Collection<UserBasic> users) {
        if (users != null) {
            values.addAll(users);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        values.clear();
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyItemChanged(values.size());
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return spanSizeLookup;
    }

    public interface Listener {
        void onUserClicked(UserBasic user, UserViewHolder userViewHolder);
    }
}
