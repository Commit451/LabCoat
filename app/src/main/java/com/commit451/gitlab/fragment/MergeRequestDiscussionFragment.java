package com.commit451.gitlab.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    ViewGroup root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listNotes;
    @BindView(R.id.send_message_view)
    SendMessageView sendMessageView;
    @BindView(R.id.progress)
    View progress;

    MergeRequestDetailAdapter adapterMergeRequestDetail;
    LinearLayoutManager layoutManagerNotes;

    Project project;
    MergeRequest mergeRequest;
    Uri nextPageUrl;
    boolean loading;
    Teleprinter teleprinter;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerNotes.getChildCount();
            int totalItemCount = layoutManagerNotes.getItemCount();
            int firstVisibleItem = layoutManagerNotes.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMoreNotes();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        project = Parcels.unwrap(getArguments().getParcelable(KEY_PROJECT));
        mergeRequest = Parcels.unwrap(getArguments().getParcelable(KEY_MERGE_REQUEST));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request_discussion, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teleprinter = new Teleprinter(getActivity());

        adapterMergeRequestDetail = new MergeRequestDetailAdapter(getActivity(), mergeRequest, project);
        layoutManagerNotes = new LinearLayoutManager(getActivity());
        listNotes.setLayoutManager(layoutManagerNotes);
        listNotes.setAdapter(adapterMergeRequestDetail);
        listNotes.addOnScrollListener(onScrollListener);

        sendMessageView.setCallback(new SendMessageView.Callback() {
            @Override
            public void onSendClicked(String message) {
                postNote(message);
            }

            @Override
            public void onAttachmentClicked() {
                Intent intent = AttachActivity.Companion.newIntent(getActivity(), project);
                ActivityOptions activityOptions = TransitionFactory.createFadeInOptions(getActivity());
                startActivityForResult(intent, REQUEST_ATTACH, activityOptions.toBundle());
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotes();
            }
        });
        loadNotes();

        App.bus().register(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ATTACH:
                if (resultCode == RESULT_OK) {
                    FileUploadResponse response = Parcels.unwrap(data.getParcelableExtra(AttachActivity.Companion.getKEY_FILE_UPLOAD_RESPONSE()));
                    progress.setVisibility(View.GONE);
                    sendMessageView.appendText(response.getMarkdown());
                } else {
                    Snackbar.make(root, R.string.failed_to_upload_file, Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        App.bus().unregister(this);
        super.onDestroyView();
    }

    private void loadNotes() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.get().getGitLab().getMergeRequestNotes(project.getId(), mergeRequest.getId())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Note>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Note> notes) {
                        swipeRefreshLayout.setRefreshing(false);
                        loading = false;
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterMergeRequestDetail.setNotes(notes);
                    }
                });
    }

    private void loadMoreNotes() {
        adapterMergeRequestDetail.setLoading(true);
        App.get().getGitLab().getMergeRequestNotes(nextPageUrl.toString())
                .compose(this.<Response<List<Note>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Note>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        adapterMergeRequestDetail.setLoading(false);
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Note> notes) {
                        adapterMergeRequestDetail.setLoading(false);
                        loading = false;
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterMergeRequestDetail.addNotes(notes);
                    }
                });
    }

    private void postNote(String message) {

        if (message.length() < 1) {
            return;
        }

        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(0.0f);
        progress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        teleprinter.hideKeyboard();
        sendMessageView.clearText();

        App.get().getGitLab().addMergeRequestNote(project.getId(), mergeRequest.getId(), message)
                .compose(this.<Note>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Note>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        progress.setVisibility(View.GONE);
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Note note) {
                        progress.setVisibility(View.GONE);
                        adapterMergeRequestDetail.addNote(note);
                        listNotes.smoothScrollToPosition(MergeRequestDetailAdapter.getHeaderCount());
                    }
                });
    }

    @Subscribe
    public void onMergeRequestChangedEvent(MergeRequestChangedEvent event) {
        if (mergeRequest.getId() == event.mergeRequest.getId()) {
            mergeRequest = event.mergeRequest;
            loadNotes();
        }
    }

}
