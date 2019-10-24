package com.michaldrabik.showly2.ui.followedshows

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FollowedPagesAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

  private val pages = mutableListOf<Fragment>()

  fun addPages(vararg fragments: Fragment) {
    pages.clear()
    fragments.forEach { pages.add(it) }
  }

  override fun getItemCount() = pages.size

  override fun createFragment(position: Int) = pages[position]
}