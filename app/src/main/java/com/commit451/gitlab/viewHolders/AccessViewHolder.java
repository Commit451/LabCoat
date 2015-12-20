package com.commit451.gitlab.viewHolders;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.commit451.gitlab.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Access denied!
 * Created by Jawn on 9/16/2015.
 */
public class AccessViewHolder extends RecyclerView.ViewHolder {

    public static AccessViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_access, parent, false);
        return new AccessViewHolder(view);
    }

    @Bind(R.id.access) TextView title;

    public AccessViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String access, int colorSelected, boolean isSelected) {
        title.setText(access);
        if (isSelected) {
            ((FrameLayout)itemView).setForeground(new ColorDrawable(colorSelected));
        } else {
            ((FrameLayout)itemView).setForeground(null);
        }
    }
}
