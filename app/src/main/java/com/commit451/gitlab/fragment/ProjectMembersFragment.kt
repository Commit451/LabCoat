package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.ProjectMembersAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.belongsToGroup
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomCompleteObserver
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder
import com.trello.rxlifecycle2.android.FragmentEvent
import io.reactivex.Single
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

class ProjectMembersFragment : ButterKnifeFragment() {

    companion object {

        fun newInstance(): ProjectMembersFragment {
            return ProjectMembersFragment()
        }
    }

    @BindView(R.id.root) lateinit var root: View
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list) lateinit var listMembers: RecyclerView
    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.add_user_button) lateinit var buttonAddUser: FloatingActionButton

    lateinit var adapterProjectMembers: ProjectMembersAdapter
    lateinit var layoutManagerMembers: GridLayoutManager

    var project: Project? = null
    var member: User? = null
    var nextPageUrl: Uri? = null
    var loading = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerMembers.childCount
            val totalItemCount = layoutManagerMembers.itemCount
            val firstVisibleItem = layoutManagerMembers.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    @OnClick(R.id.add_user_button)
    fun onAddUserClick(fab: View) {
        Navigator.navigateToAddProjectMember(activity, fab, project!!.id)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterProjectMembers = ProjectMembersAdapter(object : ProjectMembersAdapter.Listener {
            override fun onProjectMemberClicked(member: User, memberGroupViewHolder: ProjectMemberViewHolder) {
                Navigator.navigateToUser(activity, memberGroupViewHolder.image, member)
            }

            override fun onRemoveMember(member: User) {
                this@ProjectMembersFragment.member = member
                App.get().gitLab.removeProjectMember(project!!.id, member.id)
                        .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                        .subscribe(object : CustomCompleteObserver() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                Snackbar.make(root, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                                        .show()
                            }

                            override fun complete() {
                                adapterProjectMembers.removeMember(this@ProjectMembersFragment.member!!)
                            }
                        })
            }

            override fun onChangeAccess(member: User) {
                val accessDialog = AccessDialog(activity, member, project!!.id)
                accessDialog.setOnAccessChangedListener(object : AccessDialog.OnAccessChangedListener {
                    override fun onAccessChanged(member: User, accessLevel: String) {
                        loadData()
                    }
                })
                accessDialog.show()
            }

            override fun onSeeGroupClicked() {
                Navigator.navigateToGroup(activity, project!!.namespace.id)
            }
        })
        layoutManagerMembers = GridLayoutManager(activity, 2)
        layoutManagerMembers.spanSizeLookup = adapterProjectMembers.spanSizeLookup
        listMembers.layoutManager = layoutManagerMembers
        listMembers.adapter = adapterProjectMembers
        listMembers.addOnScrollListener(onScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
            setNamespace()
            loadData()
        } else {
            throw IllegalStateException("Incorrect parent activity")
        }
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    override fun loadData() {
        if (view == null) {
            return
        }

        if (project == null) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        swipeRefreshLayout.isRefreshing = true

        nextPageUrl = null
        loading = true

        load(App.get().gitLab.getProjectMembers(project!!.id))
    }

    fun loadMore() {
        if (view == null) {
            return
        }

        if (nextPageUrl == null) {
            return
        }

        swipeRefreshLayout.isRefreshing = true

        loading = true

        Timber.d("loadMore called for " + nextPageUrl!!)
        load(App.get().gitLab.getProjectMembers(nextPageUrl!!.toString()))
    }

    fun load(observable: Single<Response<List<User>>>) {
        observable
                .setup(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(object : CustomResponseSingleObserver<List<User>>() {

                    override fun error(t: Throwable) {
                        loading = false
                        Timber.e(t)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_users)
                        buttonAddUser.visibility = View.GONE
                        adapterProjectMembers.setProjectMembers(null)
                        nextPageUrl = null
                    }

                    override fun responseNonNullSuccess(members: List<User>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (!members.isEmpty()) {
                            textMessage.visibility = View.GONE
                        } else if (nextPageUrl == null) {
                            Timber.d("No project members found")
                            textMessage.setText(R.string.no_project_members)
                            textMessage.visibility = View.VISIBLE
                        }

                        buttonAddUser.visibility = View.VISIBLE

                        if (nextPageUrl == null) {
                            adapterProjectMembers.setProjectMembers(members)
                        } else {
                            adapterProjectMembers.addProjectMembers(members)
                        }

                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
                })
    }

    fun setNamespace() {
        if (project == null) {
            return
        }

        //If there is an owner, then there is no group
        if (project!!.belongsToGroup()) {
            adapterProjectMembers.setNamespace(project!!.namespace)
        } else {
            adapterProjectMembers.setNamespace(null)
        }
    }

    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        setNamespace()
        loadData()
    }

    @Subscribe
    fun onMemberAdded(event: MemberAddedEvent) {
        adapterProjectMembers.addMember(event.member)
        textMessage.visibility = View.GONE
    }
}