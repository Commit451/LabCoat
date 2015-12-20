package com.commit451.gitlab.viewHolder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A signed in account
 * Created by Jawn on 12/6/2015.
 */
public class AccountViewHolder extends RecyclerView.ViewHolder{

    public static AccountViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Bind(R.id.account_image) ImageView image;
    @Bind(R.id.account_username) TextView username;
    @Bind(R.id.account_server) TextView server;
    @Bind(R.id.account_more) View more;
    public PopupMenu popupMenu;

    private final View.OnClickListener mOnMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popupMenu.show();
        }
    };

    public AccountViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        popupMenu = new PopupMenu(itemView.getContext(), more);
        popupMenu.getMenuInflater().inflate(R.menu.item_menu_account, popupMenu.getMenu());
        more.setOnClickListener(mOnMoreClickListener);
    }

    public void bind(Account account) {
        server.setText(account.getServerUrl().toString());
        username.setText(account.getUser().getUsername());
        Uri url = ImageUtil.getAvatarUrl(account.getUser(), itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size));
        GitLabClient.getPicasso()
                .load(url)
                .into(image);
    }
}
