package com.bd.gitlab.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.model.Issue;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.views.CompoundTextView;

import com.squareup.picasso.Picasso;

import fr.tkeunebr.gravatar.Gravatar;

public class IssuesAdapter extends BaseAdapter {
	
	private ArrayList<Issue> issues;
	private LayoutInflater inflater;

	public IssuesAdapter(Context context, List<Issue> issues) {
		this.issues = new ArrayList<Issue>(issues);
		
		if(context != null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}
	
	@Override
	public int getCount() {
		return issues.size();
	}

	@Override
	public Issue getItem(int position) {
		return issues.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addIssue(Issue issue) {
		issues.add(0, issue);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) convertView = inflater.inflate(R.layout.list_item, parent, false);

		final TextView title = (TextView) convertView.findViewById(R.id.title);
		final CompoundTextView summary = (CompoundTextView) convertView.findViewById(R.id.summary);
		final TextView custom = (TextView) convertView.findViewById(R.id.custom);
		
		long tempId = issues.get(position).getIid();
		if(tempId < 1)
			tempId = issues.get(position).getId();
		
		title.setText("#" + tempId + ": " + issues.get(position).getTitle());
		
		String state = issues.get(position).getState();
		custom.setText(state);
		if(state != null && (state.equals("opened") || state.equals("reopened"))) {
			custom.setTextColor(Color.parseColor("#30C830"));
		}
		else if(state != null && (state.equals("closed"))) {
			custom.setTextColor(Color.parseColor("#FF0000"));
		}

        float percent = Repository.displayWidth / 720f;
        int size = (int) (40f * percent);
		
		String assigneeName = "Unassigned";
		String assigneeAvatarUrl = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;
		
		if(issues.get(position).getAssignee() != null) {
			assigneeName = issues.get(position).getAssignee().getName();

            if(issues.get(position).getAssignee().getEmail() != null)
                assigneeAvatarUrl = Gravatar.init().with(issues.get(position).getAssignee().getEmail()).size(size).build();
            else if(issues.get(position).getAssignee().getAvatarUrl() != null)
                assigneeAvatarUrl = issues.get(position).getAssignee().getAvatarUrl() + "&s=" + size;
		}
		
		summary.setText(assigneeName);
        Picasso.with(convertView.getContext()).load(assigneeAvatarUrl).into(summary);

		return convertView;
	}

}