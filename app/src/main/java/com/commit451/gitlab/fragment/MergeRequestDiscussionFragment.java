package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.MergeRequestDetailAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.PaginationUtil;
import com.commit451.teleprinter.Teleprinter;
import com.squareup.otto.Subscribe;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Shows the discussion of a merge request
 */
public class MergeRequestDiscussionFragment extends ButterKnifeFragment {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_MERGE_REQUEST = "merge_request";

    public static MergeRequestDiscussionFragment newInstance(Project project, MergeRequest mergeRequest) {
        MergeRequestDiscussionFragment fragment = new MergeRequestDiscussionFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_PROJECT, Parcels.wrap(project));
        args.putParcelable(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest));
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mNotesRecyclerView;
    @BindView(R.id.new_note_edit)
    EditText mNewNoteEdit;
    @BindView(R.id.progress)
    View mProgress;

    MergeRequestDetailAdapter mMergeRequestDetailAdapter;
    LinearLayoutManager mNotesLinearLayoutManager;

    Project mProject;
    MergeRequest mMergeRequest;
    Uri mNextPageUrl;
    boolean mLoading;
    Teleprinter mTeleprinter;

    EventReceiver mEventReceiver;

    @OnClick(R.id.new_note_button)
    public void onNewNoteClick() {
        postNote();
    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mNotesLinearLayoutManager.getChildCount();
            int totalItemCount = mNotesLinearLayoutManager.getItemCount();
            int firstVisibleItem = mNotesLinearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMoreNotes();
            }
        }
    };

    private EasyCallback<List<Note>> mNotesCallback = new EasyCallback<List<Note>>() {

        @Override
        public void onResponse(@NonNull List<Note> response) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mMergeRequestDetailAdapter.setNotes(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private EasyCallback<List<Note>> mMoreNotesCallback = new EasyCallback<List<Note>>() {

        @Override
        public void onResponse(@NonNull List<Note> response) {
            if (getView() == null) {
                return;
            }
            mMergeRequestDetailAdapter.setLoading(false);
            mLoading = false;
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
            mMergeRequestDetailAdapter.addNotes(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            if (getView() == null) {
                return;
            }
            mLoading = false;
            Timber.e(t, null);
            mMergeRequestDetailAdapter.setLoading(false);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private EasyCallback<Note> mPostNoteCallback = new EasyCallback<Note>() {

        @Override
        public void onResponse(@NonNull Note response) {
            if (getView() == null) {
                return;
            }
            mProgress.setVisibility(View.GONE);
            mMergeRequestDetailAdapter.addNote(response);
            mNotesRecyclerView.smoothScrollToPosition(MergeRequestDetailAdapter.getHeaderCount());
        }

        @Override
        public void onAllFailure(Throwable t) {
            if (getView() == null) {
                return;
            }
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProject = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        mMergeRequest = Parcels.unwrap(getArguments().getParcelable(KEY_MERGE_REQUEST));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request_discussion, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTeleprinter = new Teleprinter(getActivity());

        mMergeRequestDetailAdapter = new MergeRequestDetailAdapter(getActivity(), mMergeRequest);
        mNotesLinearLayoutManager = new LinearLayoutManager(getActivity());
        mNotesRecyclerView.setLayoutManager(mNotesLinearLayoutManager);
        mNotesRecyclerView.setAdapter(mMergeRequestDetailAdapter);
        mNotesRecyclerView.addOnScrollListener(mOnScrollListener);

        mNewNoteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                postNote();
                return true;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotes();
            }
        });
        loadNotes();

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    private void loadNotes() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        GitLabClient.instance().getMergeRequestNotes(mProject.getId(), mMergeRequest.getId()).enqueue(mNotesCallback);
    }

    private void loadMoreNotes() {
        mMergeRequestDetailAdapter.setLoading(true);
        GitLabClient.instance().getMergeRequestNotes(mNextPageUrl.toString()).enqueue(mMoreNotesCallback);
    }

    private void postNote() {
        String body = mNewNoteEdit.getText().toString();

        if (body.length() < 1) {
            return;
        }

        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        mTeleprinter.hideKeyboard();
        mNewNoteEdit.setText("");

        GitLabClient.instance().addMergeRequestNote(mProject.getId(), mMergeRequest.getId(), body).enqueue(mPostNoteCallback);
    }

    private class EventReceiver {

        @Subscribe
        public void onMergeRequestChangedEvent(MergeRequestChangedEvent event) {
            if (mMergeRequest.getId() == event.mergeRequest.getId()) {
                mMergeRequest = event.mergeRequest;
                loadNotes();
            }
        }
    }

}
