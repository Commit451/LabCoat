package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Breadcrumb;
import com.commit451.gitlab.viewHolders.BreadcrumbViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shows the current file path
 * Created by Jawnnypoo on 11/22/2015.
 */
public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbViewHolder> {
    private List<Breadcrumb> mValues;

    public BreadcrumbAdapter() {
        mValues = new ArrayList<>();
        notifyDataSetChanged();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            Breadcrumb breadcrumb = getValueAt(position);
            if (breadcrumb != null && breadcrumb.getListener() != null) {
                breadcrumb.getListener().onClick();
            }
        }
    };

    @Override
    public BreadcrumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BreadcrumbViewHolder holder = BreadcrumbViewHolder.newInstance(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final BreadcrumbViewHolder holder, int position) {
        String title = "";
        boolean showArrow = position != mValues.size() - 1;

        Breadcrumb breadcrumb = getValueAt(position);
        if (breadcrumb != null) {
            title = breadcrumb.getTitle();
        }

        holder.bind(title, showArrow);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(Collection<Breadcrumb> breadcrumbs) {
        mValues.clear();
        if (breadcrumbs != null) {
            mValues.addAll(breadcrumbs);
            notifyItemRangeInserted(0, breadcrumbs.size());
        }
        notifyDataSetChanged();
    }

    public Breadcrumb getValueAt(int position) {
        if (position < 0 || position >= mValues.size()) {
            return null;
        }

        return mValues.get(position);
    }
}
