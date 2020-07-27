package com.michaldrabik.showly2.ui.watchlist.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaldrabik.showly2.ui.watchlist.WatchlistFragment

class WatchlistMainPagesAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

  companion object {
    const val PAGES_COUNT = 1
  }

  override fun getItemCount() = PAGES_COUNT

  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> WatchlistFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
