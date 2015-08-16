package com.commit451.gitlab.views;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Patterns;
import android.widget.ArrayAdapter;

import com.commit451.gitlab.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Automagically fills in email accounts
 * Created by John on 8/13/15.
 */
public class EmailAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public EmailAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInEditMode()) { return; }
        Collection<String> accounts = getEmailAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    new ArrayList<>(accounts));
            setAdapter(adapter);
        }
    }

    /**
     * Get all the accounts that appear to be email accounts. HashSet so that we do not get duplicates
     * @return list of email accounts
     */
    private Set<String> getEmailAccounts() {
        HashSet<String> emailAccounts = new HashSet<>();
        AccountManager manager = (AccountManager) getContext().getSystemService(Context.ACCOUNT_SERVICE);
        final Account[] accounts = manager.getAccounts();
        for (Account account : accounts) {
            if (!TextUtils.isEmpty(account.name) && Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                emailAccounts.add(account.name);
            }
        }
        return emailAccounts;
    }
}