package com.michaldrabik.ui_base.utilities.ui

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class EqualSpacingItemDecoration : ItemDecoration {

  private var orientation = -1
  private var spanCount = -1
  private var spacing: Int
  private var halfSpacing: Int

  constructor(context: Context, @DimenRes spacingDimen: Int) {
    spacing = context.resources.getDimensionPixelSize(spacingDimen)
    halfSpacing = spacing / 2
  }

  constructor(spacingPx: Int) {
    spacing = spacingPx
    halfSpacing = spacing / 2
  }

  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
    super.getItemOffsets(outRect, view, parent, state)
    if (orientation == -1) {
      orientation = getOrientation(parent)
    }
    if (spanCount == -1) {
      spanCount = getTotalSpan(parent)
    }
    val childCount = parent.layoutManager!!.itemCount
    val childIndex = parent.getChildAdapterPosition(view)
    val itemSpanSize = getItemSpanSize(parent, childIndex)
    val spanIndex = getItemSpanIndex(parent, childIndex)

    /* INVALID SPAN */if (spanCount < 1) return
    setSpacings(outRect, parent, childCount, childIndex, itemSpanSize, spanIndex)
  }

  private fun setSpacings(outRect: Rect, parent: RecyclerView, childCount: Int, childIndex: Int, itemSpanSize: Int, spanIndex: Int) {
    outRect.top = halfSpacing
    outRect.bottom = halfSpacing
    outRect.left = halfSpacing
    outRect.right = halfSpacing
    if (isTopEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.top = spacing
    }
    if (isLeftEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.left = spacing
    }
    if (isRightEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.right = spacing
    }
    if (isBottomEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.bottom = spacing
    }
  }

  private fun getTotalSpan(parent: RecyclerView): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.spanCount
    } else if (mgr is StaggeredGridLayoutManager) {
      return mgr.spanCount
    } else if (mgr is LinearLayoutManager) {
      return 1
    }
    return -1
  }

  private fun getItemSpanSize(parent: RecyclerView, childIndex: Int): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.spanSizeLookup.getSpanSize(childIndex)
    } else if (mgr is StaggeredGridLayoutManager) {
      return 1
    } else if (mgr is LinearLayoutManager) {
      return 1
    }
    return -1
  }

  private fun getItemSpanIndex(parent: RecyclerView, childIndex: Int): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.spanSizeLookup.getSpanIndex(childIndex, spanCount)
    } else if (mgr is StaggeredGridLayoutManager) {
      return childIndex % spanCount
    } else if (mgr is LinearLayoutManager) {
      return 0
    }
    return -1
  }

  private fun getOrientation(parent: RecyclerView): Int {
    val mgr = parent.layoutManager
    if (mgr is LinearLayoutManager) {
      return mgr.orientation
    } else if (mgr is GridLayoutManager) {
      return mgr.orientation
    } else if (mgr is StaggeredGridLayoutManager) {
      return mgr.orientation
    }
    return VERTICAL
  }

  private fun isLeftEdge(parent: RecyclerView, childCount: Int, childIndex: Int, itemSpanSize: Int, spanIndex: Int): Boolean {
    return if (orientation == VERTICAL) {
      spanIndex == 0
    } else {
      childIndex == 0 || isFirstItemEdgeValid(childIndex < spanCount, parent, childIndex)
    }
  }

  private fun isRightEdge(parent: RecyclerView, childCount: Int, childIndex: Int, itemSpanSize: Int, spanIndex: Int): Boolean {
    return if (orientation == VERTICAL) {
      spanIndex + itemSpanSize == spanCount
    } else {
      isLastItemEdgeValid(childIndex >= childCount - spanCount, parent, childCount, childIndex, spanIndex)
    }
  }

  private fun isTopEdge(parent: RecyclerView, childCount: Int, childIndex: Int, itemSpanSize: Int, spanIndex: Int): Boolean {
    return if (orientation == VERTICAL) {
      childIndex == 0 || isFirstItemEdgeValid(childIndex < spanCount, parent, childIndex)
    } else {
      spanIndex == 0
    }
  }

  private fun isBottomEdge(parent: RecyclerView, childCount: Int, childIndex: Int, itemSpanSize: Int, spanIndex: Int): Boolean {
    return if (orientation == VERTICAL) {
      isLastItemEdgeValid(childIndex >= childCount - spanCount, parent, childCount, childIndex, spanIndex)
    } else {
      spanIndex + itemSpanSize == spanCount
    }
  }

  private fun isFirstItemEdgeValid(isOneOfFirstItems: Boolean, parent: RecyclerView, childIndex: Int): Boolean {
    var totalSpanArea = 0
    if (isOneOfFirstItems) {
      for (i in childIndex downTo 0) {
        totalSpanArea += getItemSpanSize(parent, i)
      }
    }
    return isOneOfFirstItems && totalSpanArea <= spanCount
  }

  private fun isLastItemEdgeValid(isOneOfLastItems: Boolean, parent: RecyclerView, childCount: Int, childIndex: Int, spanIndex: Int): Boolean {
    var totalSpanRemaining = 0
    if (isOneOfLastItems) {
      for (i in childIndex until childCount) {
        totalSpanRemaining += getItemSpanSize(parent, i)
      }
    }
    return isOneOfLastItems && totalSpanRemaining <= spanCount - spanIndex
  }

  companion object {
    private const val VERTICAL = OrientationHelper.VERTICAL
  }
}
