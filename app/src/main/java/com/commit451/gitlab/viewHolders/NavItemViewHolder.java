package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.NavItem;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jawn on 8/25/2015.
 */
public class NavItemViewHolder extends RecyclerView.ViewHolder{

    public static NavItemViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_item, parent, false);
        return new NavItemViewHolder(view);
    }

    @Bind(R.id.project_title)
    TextView title;

    public NavItemViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(NavItem navItem) {
        title.setText(navItem.title);

    }
}
