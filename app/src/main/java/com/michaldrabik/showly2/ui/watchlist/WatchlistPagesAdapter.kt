package com.michaldrabik.showly2.ui.watchlist

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingFragment
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.WatchlistMainFragment

class WatchlistPagesAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

  companion object {
    const val PAGES_COUNT = 2
  }

  override fun getItemCount() = PAGES_COUNT

  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> WatchlistMainFragment()
    1 -> WatchlistUpcomingFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
