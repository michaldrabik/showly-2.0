package com.michaldrabik.showly2.ui.common

import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class SearchViewBehaviour(private val padding: Int) : CoordinatorLayout.Behavior<FrameLayout>() {

  override fun layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
    return dependency is RecyclerView
  }

  override fun onStartNestedScroll(
    coordinatorLayout: CoordinatorLayout,
    child: FrameLayout,
    directTargetChild: View,
    target: View,
    axes: Int,
    type: Int
  ) = when (axes) {
    ViewCompat.SCROLL_AXIS_VERTICAL -> true
    else -> super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
  }

  override fun onNestedScroll(
    coordinatorLayout: CoordinatorLayout,
    child: FrameLayout,
    target: View,
    dxConsumed: Int,
    dyConsumed: Int,
    dxUnconsumed: Int,
    dyUnconsumed: Int,
    type: Int
  ) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    if (dyConsumed > 0) {
      val limit = -(child.height + 2 * padding).toFloat()
      child.translationY = (child.translationY - dyConsumed.toFloat()).coerceAtLeast(limit)
    } else if (dyConsumed <= 0) {
      child.translationY = (child.translationY - dyConsumed.toFloat()).coerceAtMost(0F)
    }
  }
}
