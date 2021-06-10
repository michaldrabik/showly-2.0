@file:Suppress("DEPRECATION")

package com.michaldrabik.ui_progress.main.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.michaldrabik.ui_progress.progress.ProgressMainFragment
import com.michaldrabik.ui_progress.recents.ProgressRecentsFragment

class ProgressRecentsAdapter(
  fragManager: FragmentManager,
  context: Context,
) : ProgressAdapter(fragManager, context) {

  override fun getItem(position: Int): Fragment = when (position) {
    0 -> ProgressMainFragment()
    1 -> ProgressRecentsFragment()
    else -> throw IllegalStateException("Unknown position")
  }
}
