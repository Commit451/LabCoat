package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Todo;
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder;
import com.commit451.gitlab.viewHolder.TodoViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Issues adapter
 */
public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_COUNT = 1;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public interface Listener {
        void onTodoClicked(Todo todo);
    }

    private Listener mListener;
    private ArrayList<Todo> mValues;
    private boolean mLoading = false;

    public TodoAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onTodoClicked(getValueAt(position));
        }
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                TodoViewHolder holder = TodoViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(onProjectClickListener);
                return holder;
            case TYPE_FOOTER:
                return LoadingFooterViewHolder.inflate(parent);
        }
        throw new IllegalStateException("No holder for view type " + viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TodoViewHolder) {
            Todo todo = getValueAt(position);
            ((TodoViewHolder) holder).bind(todo);
            holder.itemView.setTag(R.id.list_position, position);
        } else if (holder instanceof LoadingFooterViewHolder) {
            ((LoadingFooterViewHolder) holder).bind(mLoading);
        } else {
            throw new IllegalStateException("What is this holder?");
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mValues.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public void setData(Collection<Todo> todos) {
        mValues.clear();
        addData(todos);
    }

    public void addData(Collection<Todo> todos) {
        if (todos != null) {
            mValues.addAll(todos);
        }
        notifyDataSetChanged();
    }

    public Todo getValueAt(int position) {
        return mValues.get(position);
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyItemChanged(mValues.size());
    }
}
