package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.viewHolders.BreadcrumbViewHolder;

import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

/**
 * Shows the current file path
 * Created by Jawnnypoo on 11/22/2015.
 */
public class BreadcrumbAdapter extends RecyclerView.Adapter<BreadcrumbViewHolder> {

    public interface Listener {
        void onBreadcrumbClicked();
    }

    private Listener mListener;
    private ArrayList<String> mValues;

    public BreadcrumbAdapter(Listener listener) {
        mListener = listener;
        mValues = new ArrayList<>();
        clear();
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            if (position != mValues.size() && mValues.size() > 1) {
                ArrayList<String> itemsToRemove = new ArrayList<>();
                for (int i = mValues.size()-1; i > position; i--) {
                    itemsToRemove.add(mValues.get(i));
                }
                for (String item : itemsToRemove) {
                    Timber.d("Removing item " + item);
                }
                mValues.removeAll(itemsToRemove);
                notifyDataSetChanged();
                mListener.onBreadcrumbClicked();
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
        String breadcrumb = getValueAt(position);
        boolean showArrow = position != mValues.size() - 1;
        holder.bind(breadcrumb, showArrow);
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void addBreadcrumb(String breadcrumb) {
        mValues.add(breadcrumb);
        notifyDataSetChanged();
    }

    public void setData(Collection<String> breadcrumbs) {
        clear();
        if (breadcrumbs != null) {
            mValues.addAll(breadcrumbs);
            notifyItemRangeInserted(0, breadcrumbs.size());
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mValues.clear();
        mValues.add("ROOT");
        notifyDataSetChanged();
    }

    private String getValueAt(int position) {
        return mValues.get(position);
    }

    public String getCurrentPath() {
        String currentPath = "";
        if (mValues.size() > 1) {
            for (int i = 1; i < mValues.size(); i++) {
                currentPath += mValues.get(i) + "/";
            }
        }
        return currentPath;
    }
}
