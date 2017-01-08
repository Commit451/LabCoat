package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.easel.Easel;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.viewHolder.AccountFooterViewHolder;
import com.commit451.gitlab.viewHolder.AccountViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter to show all the accounts
 */
public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ACCOUNT = 0;
    private static final int TYPE_FOOTER = 1;

    private static final int FOOTER_COUNT = 1;

    private Listener listener;
    private ArrayList<Account> accounts;
    private int colorControlHighlight;

    public AccountAdapter(Context context, Listener listener) {
        this.listener = listener;
        accounts = new ArrayList<>();
        colorControlHighlight = Easel.getThemeAttrColor(context, R.attr.colorControlHighlight);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ACCOUNT:
                final AccountViewHolder holder = AccountViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        listener.onAccountClicked(getItemAtPosition(position));
                    }
                });
                return holder;
            case TYPE_FOOTER:
                AccountFooterViewHolder footerViewHolder = AccountFooterViewHolder.inflate(parent);
                footerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onAddAccountClicked();
                    }
                });
                return footerViewHolder;
        }
        throw new IllegalStateException("No known view holder for that type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AccountViewHolder) {
            final Account account = getItemAtPosition(position);
            ((AccountViewHolder) holder).bind(account, account.equals(App.get().getAccount()), colorControlHighlight);
            holder.itemView.setTag(R.id.list_position, position);
            ((AccountViewHolder) holder).mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sign_out:
                            int itemPosition = accounts.indexOf(account);
                            accounts.remove(account);
                            notifyItemRemoved(itemPosition);
                            listener.onAccountLogoutClicked(account);
                            return true;
                    }
                    return false;
                }
            });
        } else if (holder instanceof AccountFooterViewHolder) {
            //Nah
        } else {
            throw new IllegalStateException("No known bind for this viewHolder");
        }
    }

    @Override
    public int getItemCount() {
        return accounts.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position == accounts.size() ? TYPE_FOOTER : TYPE_ACCOUNT;
    }

    public void setAccounts(Collection<Account> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            this.accounts.addAll(accounts);
        }
        notifyDataSetChanged();
    }

    public void addAccount(Account account) {
        if (!accounts.contains(account)) {
            accounts.add(0, account);
            notifyItemInserted(0);
        }
    }

    private Account getItemAtPosition(int position) {
        return accounts.get(position);
    }

    public interface Listener {
        void onAccountClicked(Account account);
        void onAddAccountClicked();
        void onAccountLogoutClicked(Account account);
    }
}
