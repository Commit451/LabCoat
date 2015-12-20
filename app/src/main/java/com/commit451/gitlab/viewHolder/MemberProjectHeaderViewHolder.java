package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.MergeRequest;

import butterknife.ButterKnife;

/**
 * Header for project members
 * Created by Jawn on 12/19/2015.
 */
public class MemberProjectHeaderViewHolder extends RecyclerView.ViewHolder {

    public static MemberProjectHeaderViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_member_project, parent, false);
        return new MemberProjectHeaderViewHolder(view);
    }


    public MemberProjectHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(MergeRequest mergeRequest) {

    }
}
