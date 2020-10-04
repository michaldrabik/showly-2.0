package com.michaldrabik.ui_my_shows.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaldrabik.ui_my_shows.archive.ArchiveFragment
import com.michaldrabik.ui_my_shows.myshows.MyShowsFragment
import com.michaldrabik.ui_my_shows.seelater.SeeLaterFragment

class FollowedPagesAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

  companion object {
    const val PAGES_COUNT = 3
  }

  override fun getItemCount() = PAGES_COUNT

  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> MyShowsFragment()
    1 -> SeeLaterFragment()
    2 -> ArchiveFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
