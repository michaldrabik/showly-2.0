@file:Suppress("DEPRECATION")

package com.michaldrabik.ui_progress.main.adapters

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.michaldrabik.ui_progress.R

abstract class ProgressAdapter(
  fragManager: FragmentManager,
  private val context: Context,
) : FragmentStatePagerAdapter(fragManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

  companion object {
    const val PAGES_COUNT = 2

    const val MODE_CALENDAR = 0
    const val MODE_RECENTS = 1
  }

  override fun getCount() = PAGES_COUNT

  override fun getPageTitle(position: Int) =
    when (position) {
      0 -> context.getString(R.string.tabProgress)
      1 -> context.getString(R.string.tabCalendar)
      else -> throw IllegalStateException()
    }
}
