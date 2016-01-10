package com.commit451.gitlab.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.viewHolder.AccountFooterViewHolder;
import com.commit451.gitlab.viewHolder.AccountViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter to show all the accounts
 * Created by Jawn on 12/6/2015.
 */
public class AccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ACCOUNT = 0;
    private static final int TYPE_FOOTER = 1;

    private static final int FOOTER_COUNT = 1;

    public interface Listener {
        void onAccountClicked(Account account);
        void onAddAccountClicked();
        void onAccountLogoutClicked(Account account);
    }

    private Listener mListener;
    private ArrayList<Account> mAccounts;
    private int mColorControlHighlight;

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            mListener.onAccountClicked(getItemAtPosition(position));
        }
    };

    private View.OnClickListener mOnFooterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onAddAccountClicked();
        }
    };

    public AccountsAdapter(Context context, Listener listener) {
        mListener = listener;
        mAccounts = new ArrayList<>();
        mColorControlHighlight = Easel.getThemeAttrColor(context, R.attr.colorControlHighlight);
    }

    public void setAccounts(Collection<Account> accounts) {
        mAccounts.clear();
        if (accounts != null) {
            mAccounts.addAll(accounts);
        }
        notifyDataSetChanged();
    }

    public void addAccount(Account account) {
        if (!mAccounts.contains(account)) {
            mAccounts.add(0, account);
            notifyItemInserted(0);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ACCOUNT:
                AccountViewHolder holder = AccountViewHolder.inflate(parent);
                holder.itemView.setOnClickListener(mOnItemClickListener);
                return holder;
            case TYPE_FOOTER:
                AccountFooterViewHolder footerViewHolder = AccountFooterViewHolder.inflate(parent);
                footerViewHolder.itemView.setOnClickListener(mOnFooterClickListener);
                return footerViewHolder;
        }
        throw new IllegalStateException("No known view holder for that type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AccountViewHolder) {
            final Account account = getItemAtPosition(position);
            ((AccountViewHolder) holder).bind(account, account.equals(GitLabClient.getAccount()), mColorControlHighlight);
            holder.itemView.setTag(R.id.list_position, position);
            ((AccountViewHolder) holder).mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sign_out:
                            int itemPosition = mAccounts.indexOf(account);
                            mAccounts.remove(account);
                            notifyItemRemoved(itemPosition);
                            mListener.onAccountLogoutClicked(account);
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
        return mAccounts.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position == mAccounts.size() ? TYPE_FOOTER : TYPE_ACCOUNT;
    }

    private Account getItemAtPosition(int position) {
        return mAccounts.get(position);
    }

    public int getAccountsCount() {
        return mAccounts.size();
    }
}
