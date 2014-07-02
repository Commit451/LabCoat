package com.bd.gitlab.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.model.Project;
import com.bd.gitlab.model.User;
import com.bd.gitlab.tools.Repository;

public class DrawerAdapter extends BaseAdapter {
	
	private ArrayList<Project> projects;
	private LayoutInflater inflater;

	public DrawerAdapter(Context context, ArrayList<Project> projects) {
		this.projects = projects;
		
		if(context != null)
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return projects.size();
	}

	@Override
	public Project getItem(int position) {
		return projects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return projects.get(position).getId();
	}
	
	public int getPosition(User user) {
		return projects.indexOf(user);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) convertView = inflater.inflate(R.layout.simple_list_item, parent, false);

        final float scale = convertView.getResources().getDisplayMetrics().density;
        convertView.setMinimumHeight((int) (48 * scale + 0.5f));

        final TextView text = (TextView) convertView.findViewById(R.id.text);
		text.setText(projects.get(position).toString());

		if(Repository.selectedProject != null && Repository.selectedProject.equals(projects.get(position))) {
			text.setTextColor(Color.WHITE);
            text.setCompoundDrawablesWithIntrinsicBounds(null, null, convertView.getResources().getDrawable(R.drawable.ic_selected), null);
		}
		else {
            text.setTextColor(convertView.getResources().getColor(android.R.color.secondary_text_dark));
			text.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

		return convertView;
	}
}
