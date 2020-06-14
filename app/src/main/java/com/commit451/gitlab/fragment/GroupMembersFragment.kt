package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.aloy.DynamicGridLayoutManager
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.GroupMembersAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.fragment_group_members.*
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

class GroupMembersFragment : BaseFragment() {

    companion object {

        private const val KEY_GROUP = "group"

        fun newInstance(group: Group): GroupMembersFragment {
            val args = Bundle()
            args.putParcelable(KEY_GROUP, group)

            val fragment = GroupMembersFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapterGroupMembers: GroupMembersAdapter
    private lateinit var layoutManagerGroupMembers: DynamicGridLayoutManager

    private var member: User? = null
    private lateinit var group: Group
    private var nextPageUrl: String? = null

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerGroupMembers.childCount
            val totalItemCount = layoutManagerGroupMembers.itemCount
            val firstVisibleItem = layoutManagerGroupMembers.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !adapterGroupMembers.isLoading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    private val listener = object : GroupMembersAdapter.Listener {
        override fun onUserClicked(member: User, userViewHolder: ProjectMemberViewHolder) {
            Navigator.navigateToUser(baseActivty, userViewHolder.image, member)
        }

        override fun onUserRemoveClicked(member: User) {
            this@GroupMembersFragment.member = member
            App.get().gitLab.removeGroupMember(group.id, member.id)
                    .with(this@GroupMembersFragment)
                    .subscribe({
                        adapterGroupMembers.removeMember(this@GroupMembersFragment.member!!)
                    }, {
                        Timber.e(it)
                        Snackbar.make(root, R.string.failed_to_remove_member, Snackbar.LENGTH_SHORT)
                                .show()
                    })
        }

        override fun onUserChangeAccessClicked(member: User) {
            val accessDialog = AccessDialog(baseActivty, member, group)
            accessDialog.setOnAccessChangedListener(object : AccessDialog.OnAccessChangedListener {
                override fun onAccessChanged(member: User, accessLevel: String) {
                    loadData()
                }
            })
            accessDialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = arguments?.getParcelable(KEY_GROUP)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        adapterGroupMembers = GroupMembersAdapter(listener)
        layoutManagerGroupMembers = DynamicGridLayoutManager(baseActivty)
        layoutManagerGroupMembers.setMinimumSpanSize(baseActivty.resources.getDimensionPixelSize(R.dimen.user_list_image_size))
        layoutManagerGroupMembers.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (adapterGroupMembers.isFooter(position)) {
                    return layoutManagerGroupMembers.spanCount
                }
                return 1
            }
        }
        list.layoutManager = layoutManagerGroupMembers
        list.adapter = adapterGroupMembers
        list.addOnScrollListener(mOnScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        buttonAddUser.setOnClickListener {
            Navigator.navigateToAddGroupMember(baseActivty, buttonAddUser, group)
        }
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        App.bus().unregister(this)
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        loadGroupMembers(App.get().gitLab.getGroupMembers(group.id))
    }

    private fun loadMore() {
        if (view == null) {
            return
        }

        if (nextPageUrl == null) {
            return
        }

        swipeRefreshLayout.isRefreshing = true
        adapterGroupMembers.isLoading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        loadGroupMembers(App.get().gitLab.getProjectMembers(nextPageUrl!!.toString()))
    }

    private fun loadGroupMembers(observable: Single<Response<List<User>>>) {
        observable
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_project_members)
                    }
                    buttonAddUser.visibility = View.VISIBLE
                    if (nextPageUrl == null) {
                        adapterGroupMembers.setData(it.body)
                    } else {
                        adapterGroupMembers.addData(it.body)
                    }
                    adapterGroupMembers.isLoading = false

                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url %s", nextPageUrl)
                }, {
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error_users)
                    buttonAddUser.visibility = View.GONE
                    adapterGroupMembers.setData(null)
                })
    }

    @Suppress("unused")
    @Subscribe
    fun onMemberAdded(event: MemberAddedEvent) {
        if (view != null) {
            adapterGroupMembers.addMember(event.member)
            textMessage.visibility = View.GONE
        }
    }
}
