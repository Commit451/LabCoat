package com.commit451.gitlab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.TreeItem;

import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends BaseAdapter {
	
	private ArrayList<TreeItem> treeItems;
	private LayoutInflater inflater;

	public FilesAdapter(Context context, List<TreeItem> treeItems) {
		this.treeItems = new ArrayList<TreeItem>(treeItems);
		
		if(context != null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}
	
	@Override
	public int getCount() {
		return treeItems.size();
	}

	@Override
	public TreeItem getItem(int position) {
		return treeItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.simple_list_item, parent, false);
		}

		final TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(treeItems.get(position).getName());

		if(treeItems.get(position).getType().equals("tree")) {
			text.setCompoundDrawablesWithIntrinsicBounds(convertView.getResources().getDrawable(R.drawable.ic_folder), null, null, null);
		}
		else if(treeItems.get(position).getType().equals("submodule")) {
			text.setCompoundDrawablesWithIntrinsicBounds(convertView.getResources().getDrawable(R.drawable.ic_repo), null, null, null);
		}
		else {
			text.setCompoundDrawablesWithIntrinsicBounds(convertView.getResources().getDrawable(R.drawable.ic_doc), null, null, null);
		}

		return convertView;
	}

}
