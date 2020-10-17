package com.michaldrabik.ui_progress.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.michaldrabik.ui_progress.calendar.ProgressCalendarFragment
import com.michaldrabik.ui_progress.progress.ProgressMainFragment

class ProgressPagesAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

  companion object {
    const val PAGES_COUNT = 2
  }

  override fun getItemCount() = PAGES_COUNT

  override fun createFragment(position: Int): Fragment = when (position) {
    0 -> ProgressMainFragment()
    1 -> ProgressCalendarFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
