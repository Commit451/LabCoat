package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.rss.Entry;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Represents the view of an item in the RSS feed
 * Created by John on 10/8/15.
 */
public class FeedEntryViewHolder extends RecyclerView.ViewHolder {
    public static FeedEntryViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entry, parent, false);
        return new FeedEntryViewHolder(view);
    }

    @Bind(R.id.entry_image) public ImageView image;
    @Bind(R.id.entry_title) public TextView title;
    @Bind(R.id.entry_summary) public TextView summary;


    public FeedEntryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Entry entry) {
        GitLabClient.getPicasso()
                .load(entry.getThumbnail().getUrl())
                .into(image);

        title.setText(Html.fromHtml(entry.getTitle()));
        summary.setText(Html.fromHtml(entry.getSummary()));
    }
}
