package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Namespace;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Header view above group members
 * Created by Jawn on 12/19/2015.
 */
public class MemberGroupHeaderViewHolder extends RecyclerView.ViewHolder{

    public static MemberGroupHeaderViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_member_group, parent, false);
        return new MemberGroupHeaderViewHolder(view);
    }


    @Bind(R.id.title)
    TextView title;

    public MemberGroupHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Namespace namespace) {
        title.setText(String.format(itemView.getResources().getString(R.string.group_members), namespace.getName()));
    }
}
