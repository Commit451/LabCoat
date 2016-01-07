package com.commit451.gitlab.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.viewHolder.AssigneeSpinnerViewHolder;

import java.util.List;

/**
 * Adapter to show assignees in a spinner
 */
public class AssigneeSpinnerAdapter extends ArrayAdapter<Member> {

    public AssigneeSpinnerAdapter(Context context, List<Member> members) {
        super(context, 0, members);
        members.add(0, null);
        notifyDataSetChanged();
    }

    public int getSelectedItemPosition(UserBasic userBasic) {
        if (userBasic == null) {
            return 0;
        }
        for (int i=0; i<getCount(); i++) {
            Member member = getItem(i);
            if (member != null && userBasic.getId() == member.getId()) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getTheView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getTheView(position, convertView, parent);
    }

    private View getTheView(int position, View convertView, ViewGroup parent) {
        Member member = getItem(position);
        AssigneeSpinnerViewHolder assigneeSpinnerViewHolder;
        if (convertView == null) {
            assigneeSpinnerViewHolder = AssigneeSpinnerViewHolder.newInstance(parent);
            assigneeSpinnerViewHolder.itemView.setTag(R.id.list_view_holder, assigneeSpinnerViewHolder);
        } else {
            assigneeSpinnerViewHolder = (AssigneeSpinnerViewHolder) convertView.getTag(R.id.list_view_holder);
        }
        assigneeSpinnerViewHolder.bind(member);
        return assigneeSpinnerViewHolder.itemView;
    }

}