package com.commit451.gitlab.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.PickBranchOrTagActivity;
import com.commit451.gitlab.adapter.TagsAdapter;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Tag;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * Pick a branch, any branch
 */
public class PickTagFragment extends ButterKnifeFragment {

    private static final String EXTRA_PROJECT_ID = "project_id";
    private static final String EXTRA_CURRENT_REF = "current_ref";

    public static PickTagFragment newInstance(long projectId, @Nullable Ref ref) {
        PickTagFragment fragment = new PickTagFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_PROJECT_ID, projectId);
        args.putParcelable(EXTRA_CURRENT_REF, Parcels.wrap(ref));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.list)
    RecyclerView mProjectsListView;
    @BindView(R.id.message_text)
    TextView mMessageView;
    @BindView(R.id.progress)
    View mProgress;

    TagsAdapter mTagsAdapter;

    long mProjectId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProjectId = getArguments().getLong(EXTRA_PROJECT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pick_tag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Ref ref = Parcels.unwrap(getArguments().getParcelable(EXTRA_CURRENT_REF));
        mTagsAdapter = new TagsAdapter(ref, new TagsAdapter.Listener() {

            @Override
            public void onTagClicked(Tag entry) {
                Intent data = new Intent();
                Ref ref = new Ref(Ref.TYPE_TAG, entry.getName());
                data.putExtra(PickBranchOrTagActivity.EXTRA_REF, Parcels.wrap(ref));
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
        mProjectsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProjectsListView.setAdapter(mTagsAdapter);

        loadData();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        mProgress.setVisibility(View.VISIBLE);
        mMessageView.setVisibility(View.GONE);

        App.instance().getGitLab().getTags(mProjectId).enqueue(new EasyCallback<List<Tag>>() {
            @Override
            public void success(@NonNull List<Tag> response) {
                if (getView() == null) {
                    return;
                }
                mProgress.setVisibility(View.GONE);
                mTagsAdapter.setEntries(response);
            }

            @Override
            public void failure(Throwable t) {
                Timber.e(t, null);
                if (getView() == null) {
                    return;
                }
                mProgress.setVisibility(View.GONE);
                mMessageView.setVisibility(View.VISIBLE);
            }
        });
    }

}
