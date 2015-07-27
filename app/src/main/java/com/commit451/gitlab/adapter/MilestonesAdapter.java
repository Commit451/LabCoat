package com.commit451.gitlab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Milestone;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MilestonesAdapter extends BaseAdapter {
	
	private ArrayList<Milestone> milestones;
	private LayoutInflater inflater;
	
	public MilestonesAdapter(Context context, List<Milestone> milestones) {
		this.milestones = new ArrayList<Milestone>(milestones);
		
		if(context != null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}
	
	@Override
	public int getCount() {
		return milestones.size();
	}
	
	@Override
	public Milestone getItem(int position) {
		return milestones.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return milestones.get(position).getId();
	}
	
	public int getPosition(Milestone milestone) {
		return milestones.indexOf(milestone);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) convertView = inflater.inflate(R.layout.twoline_list_item, parent, false);

		final TextView title = (TextView) convertView.findViewById(R.id.title);
		final TextView summary = (TextView) convertView.findViewById(R.id.summary);
		
		title.setText(milestones.get(position).getTitle());
		DateFormat formatter = android.text.format.DateFormat.getDateFormat(convertView.getContext());

        if(milestones.get(position).getDueDate() != null)
            summary.setText(formatter.format(milestones.get(position).getDueDate()));
        else
            summary.setText("");

		return convertView;
	}
	
}
