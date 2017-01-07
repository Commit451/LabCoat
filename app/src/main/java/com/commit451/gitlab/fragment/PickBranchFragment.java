package com.commit451.gitlab.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.PickBranchOrTagActivity;
import com.commit451.gitlab.adapter.BranchesAdapter;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.rx.CustomSingleObserver;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Pick a branch, any branch
 */
public class PickBranchFragment extends ButterKnifeFragment {

    private static final String EXTRA_PROJECT_ID = "project_id";
    private static final String EXTRA_REF = "ref";

    public static PickBranchFragment newInstance(long projectId, @Nullable Ref ref) {
        PickBranchFragment fragment = new PickBranchFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_PROJECT_ID, projectId);
        args.putParcelable(EXTRA_REF, Parcels.wrap(ref));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.list)
    RecyclerView mProjectsListView;
    @BindView(R.id.message_text)
    TextView mMessageView;
    @BindView(R.id.progress)
    View mProgress;

    BranchesAdapter mBranchesAdapter;

    long mProjectId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProjectId = getArguments().getLong(EXTRA_PROJECT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pick_branch, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Ref existingRef = Parcels.unwrap(getArguments().getParcelable(EXTRA_REF));
        mBranchesAdapter = new BranchesAdapter(existingRef, new BranchesAdapter.Listener() {
            @Override
            public void onBranchClicked(Branch entry) {
                Intent data = new Intent();
                Ref ref = new Ref(Ref.TYPE_BRANCH, entry.getName());
                data.putExtra(PickBranchOrTagActivity.EXTRA_REF, Parcels.wrap(ref));
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
        mProjectsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProjectsListView.setAdapter(mBranchesAdapter);

        loadData();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        mProgress.setVisibility(View.VISIBLE);
        mMessageView.setVisibility(View.GONE);

        App.get().getGitLab().getBranches(mProjectId)
                .compose(this.<List<Branch>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<Branch>>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        mProgress.setVisibility(View.GONE);
                        mMessageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void success(List<Branch> branches) {
                        mProgress.setVisibility(View.GONE);
                        mBranchesAdapter.setEntries(branches);
                    }
                });
    }

}
