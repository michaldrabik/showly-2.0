package com.michaldrabik.ui_base.common

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class FastLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
  LinearLayoutManager(context, orientation, reverseLayout) {

  companion object {
    const val SPEED_RATIO = 8F
  }

  override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
    val scroller = object : LinearSmoothScroller(recyclerView?.context) {
      override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics) =
        SPEED_RATIO / displayMetrics.densityDpi
    }
    scroller.targetPosition = position
    startSmoothScroll(scroller)
  }
}
