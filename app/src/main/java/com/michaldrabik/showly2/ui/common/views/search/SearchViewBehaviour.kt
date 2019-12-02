package com.michaldrabik.showly2.ui.common.views.search

import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Note: some extra work is added because of an issue:
 * https://gist.github.com/erikhuizinga/edf408167b46eb5b1568424563ca4e59?ts=2
 */
class SearchViewBehaviour(private val padding: Int) : CoordinatorLayout.Behavior<FrameLayout>() {

  override fun layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean {
    return dependency is RecyclerView
  }

  override fun onNestedPreScroll(
    coordinatorLayout: CoordinatorLayout,
    child: FrameLayout,
    target: View,
    dx: Int,
    dy: Int,
    consumed: IntArray,
    type: Int
  ) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    stopNestedScrollIfNeeded(dy, target, type)
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
    type: Int,
    consumed: IntArray
  ) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    if (dyConsumed > 0) {
      val limit = -(child.height + 2 * padding).toFloat()
      child.translationY = (child.translationY - dyConsumed.toFloat()).coerceAtLeast(limit)
    } else if (dyConsumed <= 0) {
      child.translationY = (child.translationY - dyConsumed.toFloat()).coerceAtMost(0F)
    }
    stopNestedScrollIfNeeded(dyConsumed, target, type)
  }

  private fun stopNestedScrollIfNeeded(dy: Int, target: View, type: Int) {
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      if (dy == 0) {
        ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH)
      }
    }
  }
}
