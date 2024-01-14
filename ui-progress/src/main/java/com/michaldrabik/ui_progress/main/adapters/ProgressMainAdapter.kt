@file:Suppress("DEPRECATION")

package com.michaldrabik.ui_progress.main.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.CalendarFragment
import com.michaldrabik.ui_progress.progress.ProgressFragment

class ProgressMainAdapter(
  fragManager: FragmentManager,
  private val context: Context,
) : FragmentPagerAdapter(fragManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  companion object {
    const val PAGES_COUNT = 2
  }

  override fun getItem(position: Int): Fragment = when (position) {
    0 -> ProgressFragment()
    1 -> CalendarFragment()
    else -> throw IllegalStateException("Unknown position")
  }

  override fun getCount() = PAGES_COUNT

  override fun getPageTitle(position: Int) =
    when (position) {
      0 -> context.getString(R.string.tabProgress)
      1 -> context.getString(R.string.tabCalendar)
      else -> throw IllegalStateException()
    }
}
