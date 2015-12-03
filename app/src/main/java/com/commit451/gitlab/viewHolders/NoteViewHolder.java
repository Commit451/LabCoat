package com.commit451.gitlab.viewHolders;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Note;
import com.commit451.gitlab.tools.DateUtils;
import com.commit451.gitlab.tools.ImageUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Notes, aka comments
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
            custom.setText(DateUtils.getRelativeTimeSpanString(itemView.getContext(), note.getCreatedAt()));
        }
        if(note.getAuthor() != null) {
            title.setText(note.getAuthor().getUsername());
        }

        String temp = "";
        if(note.getBody() != null) {
            temp = note.getBody();
        }
        Bypass bypass = new Bypass(itemView.getContext());
        summary.setText(bypass.markdownToSpannable(temp));
        summary.setMovementMethod(LinkMovementMethod.getInstance());

        Uri imageUrl = ImageUtil.getAvatarUrl(note.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size));
        Picasso.with(itemView.getContext()).load(imageUrl).into(icon);
    }
}
