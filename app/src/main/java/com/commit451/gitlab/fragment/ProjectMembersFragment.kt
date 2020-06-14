package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.ProjectMembersAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.extension.belongsToGroup
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.fragment_members.*
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

class ProjectMembersFragment : BaseFragment() {

    companion object {

        fun newInstance(): ProjectMembersFragment {
            return ProjectMembersFragment()
        }
    }

    private lateinit var adapterProjectMembers: ProjectMembersAdapter
    private lateinit var layoutManagerMembers: GridLayoutManager

    private var project: Project? = null
    private var member: User? = null
    private var nextPageUrl: String? = null
    private var loading = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerMembers.childCount
            val totalItemCount = layoutManagerMembers.itemCount
            val firstVisibleItem = layoutManagerMembers.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        buttonAddUser.setOnClickListener {
            Navigator.navigateToAddProjectMember(baseActivty, buttonAddUser, project!!.id)
        }
        adapterProjectMembers = ProjectMembersAdapter(object : ProjectMembersAdapter.Listener {
            override fun onProjectMemberClicked(member: User, memberGroupViewHolder: ProjectMemberViewHolder) {
                Navigator.navigateToUser(baseActivty, memberGroupViewHolder.image, member)
            }

            override fun onRemoveMember(member: User) {
                this@ProjectMembersFragment.member = member
                App.get().gitLab.removeProjectMember(project!!.id, member.id)
                        .with(this@ProjectMembersFragment)
                        .subscribe({
                            adapterProjectMembers.removeMember(this@ProjectMembersFragment.member!!)
                        }, {
                            Timber.e(it)
                            Snackbar.make(root, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                                    .show()
                        })
            }

            override fun onChangeAccess(member: User) {
                val accessDialog = AccessDialog(baseActivty, member, project!!.id)
                accessDialog.setOnAccessChangedListener(object : AccessDialog.OnAccessChangedListener {
                    override fun onAccessChanged(member: User, accessLevel: String) {
                        loadData()
                    }
                })
                accessDialog.show()
            }

            override fun onSeeGroupClicked() {
                Navigator.navigateToGroup(baseActivty, project!!.namespace!!.id)
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

        Timber.d("loadMore called for ${nextPageUrl!!}")
        load(App.get().gitLab.getProjectMembers(nextPageUrl!!.toString()))
    }

    fun load(observable: Single<Response<List<User>>>) {
        observable
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isNotEmpty()) {
                        textMessage.visibility = View.GONE
                    } else if (nextPageUrl == null) {
                        Timber.d("No project members found")
                        textMessage.setText(R.string.no_project_members)
                        textMessage.visibility = View.VISIBLE
                    }

                    buttonAddUser.isVisible = true

                    if (nextPageUrl == null) {
                        adapterProjectMembers.setProjectMembers(it.body)
                    } else {
                        adapterProjectMembers.addProjectMembers(it.body)
                    }

                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url " + nextPageUrl)
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error_users)
                    buttonAddUser.isVisible = false
                    adapterProjectMembers.setProjectMembers(null)
                    nextPageUrl = null
                })
    }

    private fun setNamespace() {
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
