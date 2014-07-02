package com.bd.gitlab.adapter;

import fr.tkeunebr.gravatar.Gravatar;
import in.uncod.android.bypass.Bypass;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.model.Note;
import com.bd.gitlab.tools.Repository;
import com.squareup.picasso.Picasso;

public class NoteAdapter extends BaseAdapter {
	
	private ArrayList<Note> notes;
	private LayoutInflater inflater;

	public NoteAdapter(Context context, List<Note> notes) {
		this.notes = new ArrayList<Note>(notes);
		
		if(context != null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}
	
	@Override
	public int getCount() {
		return notes.size();
	}

	@Override
	public Note getItem(int position) {
		return notes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addNote(Note note) {
		if(note != null) {
			notes.add(note);
			notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) convertView = inflater.inflate(R.layout.note_list_item, parent, false);

        if(position >= notes.size() || notes.get(position) == null)
			return convertView;

		final TextView title = (TextView) convertView.findViewById(R.id.title);
        final TextView custom = (TextView) convertView.findViewById(R.id.custom);
		final TextView summary = (TextView) convertView.findViewById(R.id.summary);
        final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

        if(notes.get(position).getCreatedAt() != null)
		    custom.setText(DateUtils.getRelativeTimeSpanString(notes.get(position).getCreatedAt().getTime()));
		if(notes.get(position).getAuthor() != null)
            title.setText(notes.get(position).getAuthor().getName());

		String temp = "";
		if(notes.get(position).getBody() != null)
			temp = notes.get(position).getBody();
		Bypass bypass = new Bypass();
        summary.setText(bypass.markdownToSpannable(temp));
        summary.setMovementMethod(LinkMovementMethod.getInstance());
		
		float percent = Repository.displayWidth / 720f;
		int size = (int) (96f * percent);

        String url = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(notes.get(position).getAuthor().getEmail() != null)
            url = Gravatar.init().with(notes.get(position).getAuthor().getEmail()).size(size).build();
        else if(notes.get(position).getAuthor().getAvatarUrl() != null)
            url = notes.get(position).getAuthor().getAvatarUrl() + "&s=" + size;

        Picasso.with(convertView.getContext()).load(url).into(icon);

		return convertView;
	}

}
