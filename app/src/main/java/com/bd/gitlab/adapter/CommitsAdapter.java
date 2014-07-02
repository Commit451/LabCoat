package com.bd.gitlab.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.model.DiffLine;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.views.CompoundTextView;

import com.squareup.picasso.Picasso;

import fr.tkeunebr.gravatar.Gravatar;

public class CommitsAdapter extends BaseAdapter {

	private ArrayList<DiffLine> commits;
	private LayoutInflater inflater;
	
	public CommitsAdapter(Context context, List<DiffLine> commits) {
		this.commits = new ArrayList<DiffLine>(commits);
		
		if(context != null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}
	
	@Override
	public int getCount() {
		return commits.size();
	}
	
	@Override
	public DiffLine getItem(int position) {
		return commits.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) convertView = inflater.inflate(R.layout.list_item, parent, false);

        final TextView title = (TextView) convertView.findViewById(R.id.title);
		final CompoundTextView summary = (CompoundTextView) convertView.findViewById(R.id.summary);
		final TextView custom = (TextView) convertView.findViewById(R.id.custom);
		
		title.setText(commits.get(position).getTitle());
        summary.setText(commits.get(position).getAuthorName());
		custom.setText(DateUtils.getRelativeTimeSpanString(commits.get(position).getCreatedAt().getTime()));
		
		float percent = Repository.displayWidth / 720f;
		int size = (int) (40f * percent);

        String url = Gravatar.init().with(commits.get(position).getAuthorEmail()).size(size).build();
        Picasso.with(convertView.getContext()).load(url).into(summary);
		
		return convertView;
	}
}
