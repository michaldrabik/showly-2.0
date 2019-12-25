package com.michaldrabik.showly2.ui.followedshows

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaldrabik.showly2.ui.followedshows.myshows.MyShowsFragment
import com.michaldrabik.showly2.ui.followedshows.seelater.SeeLaterFragment

class FollowedPagesAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

  companion object {
    const val PAGES_COUNT = 2
  }

  override fun getItemCount() = PAGES_COUNT

  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> MyShowsFragment()
    1 -> SeeLaterFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
