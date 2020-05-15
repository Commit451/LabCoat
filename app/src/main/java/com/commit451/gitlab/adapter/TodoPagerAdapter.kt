package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.TodoFragment

/**
 * Projects Pager Adapter
 */
class TodoPagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles = context.resources.getStringArray(R.array.tabs_todo)

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return TodoFragment.newInstance(TodoFragment.MODE_TODO)
            1 -> return TodoFragment.newInstance(TodoFragment.MODE_DONE)
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}
