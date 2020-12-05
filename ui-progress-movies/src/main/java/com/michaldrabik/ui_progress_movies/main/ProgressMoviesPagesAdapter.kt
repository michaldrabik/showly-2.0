package com.michaldrabik.ui_progress_movies.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.michaldrabik.ui_progress_movies.R

class ProgressMoviesPagesAdapter(
  fragManager: FragmentManager,
  private val context: Context
) : FragmentStatePagerAdapter(fragManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  companion object {
    const val PAGES_COUNT = 2
  }

  override fun getCount() = PAGES_COUNT

  override fun getItem(position: Int): Fragment = when (position) {
//    0 -> ProgressMainFragment()
//    1 -> ProgressCalendarFragment()
    else -> throw IllegalStateException("Unknown position")
  }

  override fun getPageTitle(position: Int) =
    when (position) {
      0 -> context.getString(R.string.tabMoviesProgress)
      1 -> context.getString(R.string.tabMoviesCalendar)
      else -> throw IllegalStateException()
    }
}
