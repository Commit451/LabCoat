package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Note;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.tkeunebr.gravatar.Gravatar;
import in.uncod.android.bypass.Bypass;

/**
 * Created by Jawn on 8/6/2015.
 */
public class NoteViewHolder extends RecyclerView.ViewHolder{

    public static NoteViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Bind(R.id.title) TextView title;
    @Bind(R.id.summary) TextView summary;
    @Bind(R.id.custom) TextView custom;
    @Bind(R.id.icon) ImageView icon;

    public NoteViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Note note) {
        if(note.getCreatedAt() != null) {
            custom.setText(DateUtils.getRelativeTimeSpanString(note.getCreatedAt().getTime()));
        }
        if(note.getAuthor() != null) {
            title.setText(note.getAuthor().getName());
        }

        String temp = "";
        if(note.getBody() != null) {
            temp = note.getBody();
        }
        Bypass bypass = new Bypass();
        summary.setText(bypass.markdownToSpannable(temp));
        summary.setMovementMethod(LinkMovementMethod.getInstance());

        int size = itemView.getResources().getDimensionPixelSize(R.dimen.image_size);

        String url = "http://www.gravatar.com/avatar/00000000000000000000000000000000?s=" + size;

        if(note.getAuthor().getEmail() != null) {
            url = Gravatar.init().with(note.getAuthor().getEmail()).size(size).build();
        }
        else if(note.getAuthor().getAvatarUrl() != null) {
            url = note.getAuthor().getAvatarUrl() + "&s=" + size;
        }

        Picasso.with(itemView.getContext()).load(url).into(icon);
    }
}
