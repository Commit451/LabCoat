package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.viewHolders.AccessViewHolder;

/**
 * Adapter to show the access levels
 * Created by Jawn on 9/16/2015.
 */
public class AccessAdapter extends RecyclerView.Adapter<AccessViewHolder> {

    private String[] mValues;
    private String mSelectedValue;
    private int mColorControlHighlight;

    public AccessAdapter(Context context, String[] roles) {
        mColorControlHighlight = Easel.getThemeAttrColor(context, R.attr.colorControlHighlight);
        mValues = roles;
    }

    public void setSelectedAccess(String access) {
        mSelectedValue = access;
    }

    public String getValueAt(int position) {
        return mValues[position];
    }

    private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mSelectedValue = getValueAt(position);
            notifyDataSetChanged();
        }
    };

    @Override
    public AccessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AccessViewHolder holder = AccessViewHolder.create(parent);
        holder.itemView.setOnClickListener(onProjectClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final AccessViewHolder holder, int position) {
        String accessLevel = getValueAt(position);
        holder.bind(accessLevel, mColorControlHighlight, accessLevel.equalsIgnoreCase(mSelectedValue));
        holder.itemView.setTag(R.id.list_position, position);
    }

    @Override
    public int getItemCount() {
        return mValues.length;
    }

    @Nullable
    public String getSelectedValue() {
        return mSelectedValue;
    }
}
