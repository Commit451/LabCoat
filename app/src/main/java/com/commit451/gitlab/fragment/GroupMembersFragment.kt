package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import com.commit451.aloy.DynamicGridLayoutManager
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.GroupMembersAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.viewHolder.ProjectMemberViewHolder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

class GroupMembersFragment : ButterKnifeFragment() {

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

    @BindView(R.id.root)
    lateinit var root: View
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var list: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView
    @BindView(R.id.add_user_button)
    lateinit var buttonAddUser: View

    lateinit var adapterGroupMembers: GroupMembersAdapter
    lateinit var layoutManagerGroupMembers: DynamicGridLayoutManager

    var member: User? = null
    lateinit var group: Group
    var nextPageUrl: Uri? = null

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
        layoutManagerGroupMembers.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
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

        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        App.bus().unregister(this)
    }

    @OnClick(R.id.add_user_button)
    fun onAddUserClick(fab: View) {
        Navigator.navigateToAddGroupMember(baseActivty, fab, group)
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
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<User>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_users)
                        buttonAddUser.visibility = View.GONE
                        adapterGroupMembers.setData(null)
                    }

                    override fun responseNonNullSuccess(members: List<User>) {
                        swipeRefreshLayout.isRefreshing = false
                        if (members.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_project_members)
                        }
                        buttonAddUser.visibility = View.VISIBLE
                        if (nextPageUrl == null) {
                            adapterGroupMembers.setData(members)
                        } else {
                            adapterGroupMembers.addData(members)
                        }
                        adapterGroupMembers.isLoading = false

                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url %s", nextPageUrl)
                    }
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
