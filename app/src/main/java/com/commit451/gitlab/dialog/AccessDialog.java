package com.commit451.gitlab.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.rx.CustomSingleObserver;

import java.util.Arrays;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Change a users access level, either for a group or for a project
 */
public class AccessDialog extends MaterialDialog {

    void onApply() {
        if (getSelectedIndex() == -1) {
            Toast.makeText(getContext(), R.string.please_select_access_level, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        String accessLevel = mRoleNames[getSelectedIndex()];
        if (accessLevel == null) {
            Toast.makeText(getContext(), R.string.please_select_access_level, Toast.LENGTH_LONG)
                    .show();
        } else {
            changeAccess(Member.getAccessLevel(accessLevel));
        }
    }

    void onCancel() {
        dismiss();
    }

    OnAccessChangedListener mAccessChangedListener;
    Listener mAccessAppliedListener;

    String[] mRoleNames;
    long mProjectId = -1;
    Group mGroup;
    Member mMember;

    public AccessDialog(Context context, Listener accessAppliedListener) {
        this(context, null, null, -1);
        mAccessAppliedListener = accessAppliedListener;
    }

    public AccessDialog(Context context, Member member, Group group) {
        this(context, member, group, -1);
    }

    public AccessDialog(Context context, Member member, long projectId) {
        this(context, member, null, projectId);
    }

    private AccessDialog(Context context, Member member, Group group, long projectId) {
        super(new MaterialDialog.Builder(context)
                .items((group == null) ? R.array.project_role_names : R.array.group_role_names)
                .itemsCallbackSingleChoice(-1, new ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        return true;
                    }
                })
                .theme(Theme.DARK)
                .progress(true, 0) // So we can later show loading progress
                .positiveText(R.string.action_apply)
                .negativeText(R.string.md_cancel_label));
        mRoleNames = getContext().getResources().getStringArray((group == null)
                ? R.array.project_role_names
                : R.array.group_role_names);
        getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onApply();
            }
        });
        getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancel();
            }
        });
        mMember = member;
        mGroup = group;
        mProjectId = projectId;
        if (mMember != null) {
            setSelectedIndex(Arrays.asList(mRoleNames).indexOf(
                    Member.getAccessLevel(mMember.getAccessLevel())));
        }
    }

    private void changeAccess(int accessLevel) {

        if (mGroup != null) {
            showLoading();
            editGroupOrProjectMember(App.get().getGitLab().editGroupMember(mGroup.getId(), mMember.getId(), accessLevel));
        } else if (mProjectId != -1) {
            showLoading();
            editGroupOrProjectMember(App.get().getGitLab().editProjectMember(mProjectId, mMember.getId(), accessLevel));
        } else if (mAccessAppliedListener != null) {
            mAccessAppliedListener.onAccessApplied(accessLevel);
        } else {
            throw new IllegalStateException("Not sure what to apply this access change to. Check the constructors plz");
        }
    }

    private void editGroupOrProjectMember(Single<Member> observable) {
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Member>() {

                    @Override
                    public void error(Throwable t) {
                        Timber.e(t);
                        AccessDialog.this.onError();
                    }

                    @Override
                    public void success(Member member) {
                        if (mAccessChangedListener != null) {
                            mAccessChangedListener.onAccessChanged(mMember, mRoleNames[getSelectedIndex()]);
                        }
                        dismiss();
                    }
                });
    }

    public void showLoading() {
        getActionButton(DialogAction.POSITIVE).setEnabled(false);
    }

    private void onError() {
        Toast.makeText(getContext(), R.string.failed_to_apply_access_level, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public void setOnAccessChangedListener(OnAccessChangedListener listener) {
        mAccessChangedListener = listener;
    }

    public interface OnAccessChangedListener {
        void onAccessChanged(Member member, String accessLevel);
    }

    public interface Listener {
        void onAccessApplied(int accessLevel);
    }
}
