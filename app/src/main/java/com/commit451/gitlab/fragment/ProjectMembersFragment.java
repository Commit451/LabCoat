package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.ProjectMembersAdapter;
import com.commit451.gitlab.dialog.AccessDialog;
import com.commit451.gitlab.event.MemberAddedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class ProjectMembersFragment extends ButterKnifeFragment {

    public static ProjectMembersFragment newInstance() {
        return new ProjectMembersFragment();
    }

    @BindView(R.id.root)
    View root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listMembers;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.add_user_button)
    FloatingActionButton buttonAddUser;

    ProjectMembersAdapter adapterProjectMembers;
    GridLayoutManager layoutManagerMembers;

    Project project;
    Member member;
    Uri nextPageUrl;
    boolean loading = false;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerMembers.getChildCount();
            int totalItemCount = layoutManagerMembers.getItemCount();
            int firstVisibleItem = layoutManagerMembers.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterProjectMembers = new ProjectMembersAdapter(new ProjectMembersAdapter.Listener() {
            @Override
            public void onProjectMemberClicked(Member member, ProjectMemberViewHolder memberGroupViewHolder) {
                Navigator.navigateToUser(getActivity(), memberGroupViewHolder.image, member);
            }

            @Override
            public void onRemoveMember(Member member) {
                ProjectMembersFragment.this.member = member;
                App.get().getGitLab().removeProjectMember(project.getId(), member.getId())
                        .compose(ProjectMembersFragment.this.<String>bindToLifecycle())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CustomSingleObserver<String>() {

                            @Override
                            public void error(@NonNull Throwable t) {
                                Timber.e(t);
                                Snackbar.make(root, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void success(@NonNull String s) {
                                adapterProjectMembers.removeMember(ProjectMembersFragment.this.member);
                            }
                        });
            }

            @Override
            public void onChangeAccess(Member member) {
                AccessDialog accessDialog = new AccessDialog(getActivity(), member, project.getId());
                accessDialog.setOnAccessChangedListener(new AccessDialog.OnAccessChangedListener() {
                    @Override
                    public void onAccessChanged(Member member, String accessLevel) {
                        loadData();
                    }
                });
                accessDialog.show();
            }

            @Override
            public void onSeeGroupClicked() {
                Navigator.navigateToGroup(getActivity(), project.getNamespace().getId());
            }
        });
        layoutManagerMembers = new GridLayoutManager(getActivity(), 2);
        layoutManagerMembers.setSpanSizeLookup(adapterProjectMembers.getSpanSizeLookup());
        listMembers.setLayoutManager(layoutManagerMembers);
        listMembers.setAdapter(adapterProjectMembers);
        listMembers.addOnScrollListener(onScrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            project = ((ProjectActivity) getActivity()).getProject();
            setNamespace();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        App.bus().unregister(this);
        super.onDestroyView();
    }

    @OnClick(R.id.add_user_button)
    public void onAddUserClick(View fab) {
        Navigator.navigateToAddProjectMember(getActivity(), fab, project.getId());
    }

    @Override
    public void loadData() {
        if (getView() == null) {
            return;
        }

        if (project == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        nextPageUrl = null;
        loading = true;

        load(App.get().getGitLab().getProjectMembers(project.getId()));
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (nextPageUrl == null) {
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        loading = true;

        Timber.d("loadMore called for " + nextPageUrl);
        load(App.get().getGitLab().getProjectMembers(nextPageUrl.toString()));
    }

    private void load(Single<Response<List<Member>>> observable) {
        observable
                .compose(this.<Response<List<Member>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Member>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        loading = false;
                        Timber.e(t);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_users);
                        buttonAddUser.setVisibility(View.GONE);
                        adapterProjectMembers.setProjectMembers(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Member> members) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (!members.isEmpty()) {
                            textMessage.setVisibility(View.GONE);
                        } else if (nextPageUrl == null) {
                            Timber.d("No project members found");
                            textMessage.setText(R.string.no_project_members);
                            textMessage.setVisibility(View.VISIBLE);
                        }

                        buttonAddUser.setVisibility(View.VISIBLE);

                        if (nextPageUrl == null) {
                            adapterProjectMembers.setProjectMembers(members);
                        } else {
                            adapterProjectMembers.addProjectMembers(members);
                        }

                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url " + nextPageUrl);
                    }
                });
    }

    private void setNamespace() {
        if (project == null) {
            return;
        }

        //If there is an owner, then there is no group
        if (project.belongsToGroup()) {
            adapterProjectMembers.setNamespace(project.getNamespace());
        } else {
            adapterProjectMembers.setNamespace(null);
        }
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        setNamespace();
        loadData();
    }

    @Subscribe
    public void onMemberAdded(MemberAddedEvent event) {
        if (adapterProjectMembers != null) {
            adapterProjectMembers.addMember(event.member);

            if (getView() != null) {
                textMessage.setVisibility(View.GONE);
            }
        }
    }
}