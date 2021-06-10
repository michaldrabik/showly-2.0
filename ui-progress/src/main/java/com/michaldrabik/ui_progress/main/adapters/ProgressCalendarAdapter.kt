@file:Suppress("DEPRECATION")

package com.michaldrabik.ui_progress.main.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.michaldrabik.ui_progress.calendar.ProgressCalendarFragment
import com.michaldrabik.ui_progress.progress.ProgressMainFragment

class ProgressCalendarAdapter(
  fragManager: FragmentManager,
  context: Context,
) : ProgressAdapter(fragManager, context) {

  override fun getItem(position: Int): Fragment = when (position) {
    0 -> ProgressMainFragment()
    1 -> ProgressCalendarFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
