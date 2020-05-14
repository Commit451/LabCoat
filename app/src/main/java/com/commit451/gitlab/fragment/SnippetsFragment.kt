package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ProjectActivity
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.event.ProjectReloadEvent
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.Snippet
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.SnippetViewHolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_commits.*
import kotlinx.android.synthetic.main.fragment_snippets.*
import kotlinx.android.synthetic.main.fragment_snippets.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_snippets.textMessage
import org.greenrobot.eventbus.Subscribe

class SnippetsFragment : BaseFragment() {

    companion object {

        fun newInstance(): SnippetsFragment {
            return SnippetsFragment()
        }
    }

    private lateinit var state: String
    private lateinit var states: Array<String>
    private var project: Project? = null

    private lateinit var adapter: BaseAdapter<Snippet, SnippetViewHolder>
    private lateinit var loadHelper: LoadHelper<Snippet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = resources.getString(R.string.milestone_state_value_default)
        states = resources.getStringArray(R.array.milestone_state_values)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_snippets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.bus().register(this)

        buttonAdd.setOnClickListener {
            if (project != null) {
                Navigator.navigateToAddMilestone(baseActivty, buttonAdd, project!!)
            } else {
                Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                        .show()
            }
        }

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = SnippetViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val snippet = adapter.items[viewHolder.adapterPosition]
                        //do something!
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listCommits,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.getSnippets(project!!.id)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        spinnerState.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, resources.getStringArray(R.array.milestone_state_names))
        spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                state = states[position]
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (activity is ProjectActivity) {
            project = (activity as ProjectActivity).project
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
        loadHelper.load()
    }

    @Suppress("unused")
    @Subscribe
    fun onProjectReload(event: ProjectReloadEvent) {
        project = event.project
        loadData()
    }
}
