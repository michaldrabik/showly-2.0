package com.michaldrabik.showly2.utilities

import android.view.View

private const val SAFE_INTERVAL = 1000

class SafeOnClickListener(private val action: (view: View) -> Unit) : View.OnClickListener {
  private var lastClickTimestamp = 0L

  override fun onClick(clickedView: View) {
    val currentTimestamp = System.currentTimeMillis()
    if (lastClickTimestamp == 0L || currentTimestamp - lastClickTimestamp > SAFE_INTERVAL) {
      action(clickedView)
      lastClickTimestamp = currentTimestamp
    }
  }
}