package com.michaldrabik.showly2.ui.common.views.search

import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class DiscoverChipsViewBehaviour : CoordinatorLayout.Behavior<ViewGroup>() {

  override fun layoutDependsOn(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
    return dependency is RecyclerView
  }

  override fun onStartNestedScroll(
    coordinatorLayout: CoordinatorLayout,
    child: ViewGroup,
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
    child: ViewGroup,
    target: View,
    dxConsumed: Int,
    dyConsumed: Int,
    dxUnconsumed: Int,
    dyUnconsumed: Int,
    type: Int
  ) {
    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    child.translationY = (child.translationY - dyConsumed.toFloat()).coerceAtMost(0F)
  }
}
