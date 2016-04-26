package com.commit451.gitlab.viewHolder;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.commit451.gitlab.R;
import com.commit451.gitlab.util.AppThemeUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows an access level
 */
public class AccessViewHolder extends RecyclerView.ViewHolder {

    public static AccessViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_access, parent, false);
        return new AccessViewHolder(view);
    }

    @BindView(R.id.access) TextView mTitleView;

    public AccessViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String access, int colorSelected, boolean isSelected) {
        mTitleView.setText(access);
        ((FrameLayout) itemView).setForeground(isSelected ? new ColorDrawable(colorSelected) : null);
    }
}
