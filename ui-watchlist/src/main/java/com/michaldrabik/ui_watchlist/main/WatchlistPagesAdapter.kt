package com.michaldrabik.ui_watchlist.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaldrabik.ui_watchlist.upcoming.WatchlistUpcomingFragment
import com.michaldrabik.ui_watchlist.watchlist.WatchlistMainFragment

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
