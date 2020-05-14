package com.commit451.gitlab.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.ProjectViewHolder
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_projects.*
import kotlinx.android.synthetic.main.fragment_projects.swipeRefreshLayout
import retrofit2.Response

class ProjectsFragment : BaseFragment() {

    companion object {

        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_QUERY = "extra_query"
        private const val EXTRA_GROUP = "extra_group"

        const val MODE_ALL = 0
        const val MODE_MINE = 1
        const val MODE_STARRED = 2
        const val MODE_SEARCH = 3
        const val MODE_GROUP = 4

        fun newInstance(mode: Int): ProjectsFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, mode)

            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(searchTerm: String): ProjectsFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, MODE_SEARCH)
            args.putString(EXTRA_QUERY, searchTerm)
            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(group: Group): ProjectsFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, MODE_GROUP)
            args.putParcelable(EXTRA_GROUP, group)
            val fragment = ProjectsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mode: Int = 0
    private var query: String? = null
    private var listener: Listener? = null
    private lateinit var colors: IntArray

    private lateinit var adapter: BaseAdapter<Project, ProjectViewHolder>
    private lateinit var loadHelper: LoadHelper<Project>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getInt(EXTRA_MODE)!!
        query = arguments?.getString(EXTRA_QUERY)
        colors = requireContext().resources.getIntArray(R.array.cool_colors)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = ProjectViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val project = adapter.items[viewHolder.adapterPosition]
                        if (listener == null) {
                            Navigator.navigateToProject(baseActivty, project)
                        } else {
                            listener?.onProjectClicked(project)
                        }
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, position, item ->
                    val color = colors[position % colors.size]
                    viewHolder.bind(item, color)
                }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listProjects,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    mapModeToSingle()
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        listProjects.addItemDecoration(DividerItemDecoration(baseActivty))

        loadData()
    }

    override fun loadData() {
        loadHelper.load()
    }

    private fun mapModeToSingle(): Single<Response<List<Project>>> {
        return when (mode) {
            MODE_ALL -> {
                gitLab().getAllProjects()
            }
            MODE_MINE -> {
                gitLab().getMyProjects(baseActivty.account.username!!)
            }
            MODE_STARRED -> {
                gitLab().getStarredProjects()
            }
            MODE_SEARCH -> if (query != null) {
                gitLab().searchAllProjects(query!!)
            } else {
                Single.never()
            }
            MODE_GROUP -> {
                val group = arguments?.getParcelable<Group>(EXTRA_GROUP)
                        ?: throw IllegalStateException("You must also pass a group if you want to show a groups projects")
                gitLab().getGroupProjects(group.id)
            }
            else -> throw IllegalStateException("$mode is not defined")
        }
    }

    fun searchQuery(query: String) {
        this.query = query
        adapter.clear()
        loadData()
    }

    private fun gitLab(): GitLab {
        return listener?.providedGitLab() ?: App.get().gitLab
    }

    interface Listener {
        fun onProjectClicked(project: Project)

        /**
         * We need this for configuring widgets
         */
        fun providedGitLab(): GitLab
    }
}
