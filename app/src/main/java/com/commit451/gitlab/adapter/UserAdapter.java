package com.commit451.gitlab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import fr.tkeunebr.gravatar.Gravatar;

public class UserAdapter extends BaseAdapter {
	
	private ArrayList<User> users;
	private LayoutInflater inflater;
	
	public UserAdapter(Context context, List<User> users) {
		this.users = new ArrayList<User>(users);
		
		if(context != null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}
	
	@Override
	public int getCount() {
		return users.size();
	}
	
	@Override
	public User getItem(int position) {
		return users.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		if(users.get(position) == null)
			return -1;
		
		return users.get(position).getId();
	}
	
	public int getPosition(User user) {
		return users.indexOf(user);
	}
	
	public void addUser(User user) {
		users.add(user);
		notifyDataSetChanged();
	}
	
	public void removeUser(long userId) {
		for(User u : users) {
			if(u.getId() == userId) {
				users.remove(u);
				break;
			}
		}
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) convertView = inflater.inflate(R.layout.user_list_item, parent, false);

        final TextView title = (TextView) convertView.findViewById(R.id.title);
		final TextView summary = (TextView) convertView.findViewById(R.id.summary);
		final TextView custom = (TextView) convertView.findViewById(R.id.custom);
        final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		
		title.setText(users.get(position).getName());
		if(users.get(position).getEmail() != null)
            summary.setText(users.get(position).getEmail());
        else
            summary.setText(users.get(position).getUsername());
		
		custom.setText(users.get(position).getAccessLevel(convertView.getResources().getStringArray(R.array.role_names)));
		
		float percent = Repository.displayWidth / 720f;
		int size = (int) (96f * percent);

        String url = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(users.get(position).getEmail() != null)
            url = Gravatar.init().with(users.get(position).getEmail()).size(size).build();
        else if(users.get(position).getAvatarUrl() != null)
            url = users.get(position).getAvatarUrl() + "&s=" + size;

        Picasso.with(convertView.getContext()).load(url).into(icon);
		
		return convertView;
	}
}
