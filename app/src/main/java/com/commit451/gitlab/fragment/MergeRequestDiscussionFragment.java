package com.commit451.gitlab.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.AttachActivity;
import com.commit451.gitlab.adapter.MergeRequestDetailAdapter;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.model.api.FileUploadResponse;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.TransitionFactory;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.view.SendMessageView;
import com.commit451.teleprinter.Teleprinter;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Shows the discussion of a merge request
 */
public class MergeRequestDiscussionFragment extends ButterKnifeFragment {

    private static final String KEY_PROJECT = "project";
    private static final String KEY_MERGE_REQUEST = "merge_request";

    private static final int REQUEST_ATTACH = 1;

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
    @BindView(R.id.send_message_view)
    SendMessageView mSendMessageView;
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

        mMergeRequestDetailAdapter = new MergeRequestDetailAdapter(getActivity(), mMergeRequest, mProject);
        mNotesLinearLayoutManager = new LinearLayoutManager(getActivity());
        mNotesRecyclerView.setLayoutManager(mNotesLinearLayoutManager);
        mNotesRecyclerView.setAdapter(mMergeRequestDetailAdapter);
        mNotesRecyclerView.addOnScrollListener(mOnScrollListener);

        mSendMessageView.setCallbacks(new SendMessageView.Callbacks() {
            @Override
            public void onSendClicked(String message) {
                postNote(message);
            }

            @Override
            public void onAttachmentClicked() {
                Intent intent = AttachActivity.newIntent(getActivity(), mProject);
                ActivityOptions activityOptions = TransitionFactory.createFadeInOptions(getActivity());
                startActivityForResult(intent, REQUEST_ATTACH, activityOptions.toBundle());
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
        App.bus().register(mEventReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ATTACH:
                if (resultCode == RESULT_OK) {
                    FileUploadResponse response = Parcels.unwrap(data.getParcelableExtra(AttachActivity.KEY_FILE_UPLOAD_RESPONSE));
                    mProgress.setVisibility(View.GONE);
                    mSendMessageView.appendText(response.getMarkdown());
                } else {
                    Snackbar.make(mRoot, R.string.failed_to_upload_file, Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.bus().unregister(mEventReceiver);
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
        App.get().getGitLab().getMergeRequestNotes(mProject.getId(), mMergeRequest.getId())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Note>>() {

                    @Override
                    public void error(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(List<Note> notes) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoading = false;
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mMergeRequestDetailAdapter.setNotes(notes);
                    }
                });
    }

    private void loadMoreNotes() {
        mMergeRequestDetailAdapter.setLoading(true);
        App.get().getGitLab().getMergeRequestNotes(mNextPageUrl.toString())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Note>>() {

                    @Override
                    public void error(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mMergeRequestDetailAdapter.setLoading(false);
                        Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(List<Note> notes) {
                        mMergeRequestDetailAdapter.setLoading(false);
                        mLoading = false;
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mMergeRequestDetailAdapter.addNotes(notes);
                    }
                });
    }

    private void postNote(String message) {

        if (message.length() < 1) {
            return;
        }

        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        mTeleprinter.hideKeyboard();
        mSendMessageView.clearText();

        App.get().getGitLab().addMergeRequestNote(mProject.getId(), mMergeRequest.getId(), message)
                .compose(this.<Note>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Note>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        mProgress.setVisibility(View.GONE);
                        Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(Note note) {
                        mProgress.setVisibility(View.GONE);
                        mMergeRequestDetailAdapter.addNote(note);
                        mNotesRecyclerView.smoothScrollToPosition(MergeRequestDetailAdapter.getHeaderCount());
                    }
                });
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
