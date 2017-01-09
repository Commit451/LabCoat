package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import butterknife.*
import com.commit451.alakazam.HideRunnable
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.UserAdapter
import com.commit451.gitlab.dialog.AccessDialog
import com.commit451.gitlab.event.MemberAddedEvent
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.Member
import com.commit451.gitlab.model.api.UserBasic
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.viewHolder.UserViewHolder
import com.commit451.teleprinter.Teleprinter
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.parceler.Parcels
import retrofit2.Response
import timber.log.Timber

/**
 * Add a new user to the repo or to the group, depending on the mode
 */
class AddUserActivity : MorphActivity() {

    companion object {

        private val KEY_PROJECT_ID = "project_id"
        private val KEY_GROUP = "group"

        fun newIntent(context: Context, projectId: Long): Intent {
            val intent = Intent(context, AddUserActivity::class.java)
            intent.putExtra(KEY_PROJECT_ID, projectId)
            return intent
        }

        fun newIntent(context: Context, group: Group): Intent {
            val intent = Intent(context, AddUserActivity::class.java)
            intent.putExtra(KEY_GROUP, Parcels.wrap(group))
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.search)
    lateinit var textSearch: EditText
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var list: RecyclerView
    @BindView(R.id.clear)
    lateinit var buttonClear: View

    lateinit var layoutManager: GridLayoutManager
    lateinit var adapter: UserAdapter
    lateinit var dialogAccess: AccessDialog
    lateinit var teleprinter: Teleprinter

    var projectId: Long = 0
    var group: Group? = null
    var query: String? = null
    var nextPageUrl: Uri? = null
    var loading = false
    var selectedUser: UserBasic? = null

    @OnClick(R.id.clear)
    fun onClearClick() {
        buttonClear.animate().alpha(0.0f).withEndAction {
            buttonClear.visibility = View.GONE
            textSearch.text.clear()
            teleprinter.showKeyboard(textSearch)
        }
    }

    @OnEditorAction(R.id.search)
    internal fun onEditorAction(): Boolean {
        if (!TextUtils.isEmpty(textSearch.text)) {
            query = textSearch.text.toString()
            loadData()
        }
        return true
    }

    @OnTextChanged(R.id.search)
    internal fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (TextUtils.isEmpty(s)) {
            buttonClear.animate()
                    .alpha(0.0f)
                    .withEndAction(HideRunnable(buttonClear))
        } else {
            buttonClear.visibility = View.VISIBLE
            buttonClear.animate().alpha(1.0f)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)
        ButterKnife.bind(this)
        teleprinter = Teleprinter(this)
        projectId = intent.getLongExtra(KEY_PROJECT_ID, -1)
        group = Parcels.unwrap<Group>(intent.getParcelableExtra<Parcelable>(KEY_GROUP))
        dialogAccess = AccessDialog(this, AccessDialog.Listener { accessLevel ->
            dialogAccess.showLoading()
            if (group == null) {
                add(App.get().gitLab.addProjectMember(projectId, selectedUser!!.id, accessLevel))
            } else {
                add(App.get().gitLab.addGroupMember(projectId, selectedUser!!.id, accessLevel))
            }
        })
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        adapter = UserAdapter(object : UserAdapter.Listener {
            override fun onUserClicked(user: UserBasic, userViewHolder: UserViewHolder) {
                selectedUser = user
                dialogAccess.show()
            }
        })
        swipeRefreshLayout.setOnRefreshListener { loadData() }
        list.adapter = adapter
        layoutManager = GridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = adapter.spanSizeLookup
        list.layoutManager = layoutManager
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                    loadMore()
                }
            }
        })

        morph(root)
    }

    private fun loadData() {
        teleprinter.hideKeyboard()
        swipeRefreshLayout.isRefreshing = true
        loading = true
        App.get().gitLab.searchUsers(query)
                .compose(this.bindToLifecycle<Response<List<UserBasic>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<UserBasic>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        swipeRefreshLayout.isRefreshing = false
                        loading = false
                        Snackbar.make(root, getString(R.string.connection_error_users), Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseSuccess(users: List<UserBasic>) {
                        swipeRefreshLayout.isRefreshing = false
                        loading = false
                        adapter.setData(users)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url is %s", nextPageUrl)
                    }
                })
    }

    private fun loadMore() {
        loading = true
        adapter.setLoading(true)
        Timber.d("loadMore " + nextPageUrl!!.toString() + " " + query)
        App.get().gitLab.searchUsers(nextPageUrl!!.toString(), query)
                .compose(this.bindToLifecycle<Response<List<UserBasic>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<UserBasic>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        adapter.setLoading(false)
                    }

                    override fun responseSuccess(users: List<UserBasic>) {
                        loading = false
                        adapter.setLoading(false)
                        adapter.addData(users)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                    }
                })
    }

    private fun add(observable: Single<Response<Member>>) {
        observable.subscribeOn(Schedulers.io())
                .compose(this.bindToLifecycle<Response<Member>>())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<Member>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        var message = getString(R.string.error_failed_to_add_user)
                        if (t is HttpException) {
                            when (t.code()) {
                                409 -> message = getString(R.string.error_user_conflict)
                            }
                        }
                        Snackbar.make(root, message, Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun responseSuccess(member: Member) {
                        Snackbar.make(root, R.string.user_added_successfully, Snackbar.LENGTH_SHORT)
                                .show()
                        dialogAccess.dismiss()
                        dismiss()
                        App.bus().post(MemberAddedEvent(member))
                    }
                })
    }
}
