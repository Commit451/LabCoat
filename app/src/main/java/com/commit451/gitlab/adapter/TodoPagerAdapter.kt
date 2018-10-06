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
class TodoPagerAdapter(context: Context, fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    private val titles: Array<String> = context.resources.getStringArray(R.array.tabs_todo)

    override fun getItem(position: Int): androidx.fragment.app.Fragment {

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
