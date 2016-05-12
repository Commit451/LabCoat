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
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public interface Listener {
        void onUserClicked(UserBasic user, UserViewHolder userViewHolder);
    }
    private Listener mListener;
    private ArrayList<UserBasic> mData;
    private boolean mLoading = false;

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            UserViewHolder holder = (UserViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onUserClicked(getUser(position), holder);
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

    public UsersAdapter(Listener listener) {
        mListener = listener;
        mData = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                UserViewHolder holder = UserViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(mItemClickListener);
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No known viewholder for type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(mData.get(position));
            holder.itemView.setTag(R.id.list_position, position);
            holder.itemView.setTag(R.id.list_view_holder, holder);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(mLoading);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mData.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + FOOTER_COUNT;
    }

    private UserBasic getUser(int position) {
        return mData.get(position);
    }

    public void setData(Collection<UserBasic> users) {
        mData.clear();
        addData(users);
    }

    public void addData(Collection<UserBasic> users) {
        if (users != null) {
            mData.addAll(users);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyItemChanged(mData.size());
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return mSpanSizeLookup;
    }
}
