package com.michaldrabik.ui_my_shows.common.layout

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_my_shows.common.views.CollectionShowCompactView
import com.michaldrabik.ui_my_shows.common.views.CollectionShowView

class CollectionShowListItemDecoration : ItemDecoration {

  private var spacing: Int
  private var halfSpacing: Int
  private val isTablet: Boolean

  constructor(
    context: Context,
    @DimenRes spacingDimen: Int,
  ) {
    this.spacing = context.resources.getDimensionPixelSize(spacingDimen)
    this.halfSpacing = spacing / 2
    this.isTablet = context.isTablet()
  }

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State,
  ) {
    if (view !is CollectionShowView && view !is CollectionShowCompactView) {
      return
    }
    if (!isTablet && (parent.layoutManager is LinearLayoutManager)) {
      getItemOffsetsPhone(outRect, view)
      return
    }
    if (isTablet && (parent.layoutManager is GridLayoutManager)) {
      getItemOffsetsTablet(outRect, view, parent)
      return
    }
  }

  private fun getItemOffsetsTablet(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
  ) {
    if (view is CollectionShowView) {
      outRect.top = spacing
      outRect.bottom = spacing
    } else if (view is CollectionShowCompactView) {
      outRect.top = halfSpacing
      outRect.bottom = halfSpacing
    }

    val totalSpan = (parent.layoutManager as GridLayoutManager).spanCount
    val column = getPosition(parent, view) % totalSpan

    outRect.left = (spacing * 2) * column / totalSpan
    outRect.right = (spacing * 2) * ((totalSpan - 1) - column) / totalSpan
  }

  private fun getItemOffsetsPhone(outRect: Rect, view: View) {
    if (view is CollectionShowView) {
      outRect.top = spacing
      outRect.bottom = spacing
    } else if (view is CollectionShowCompactView) {
      outRect.top = halfSpacing
      outRect.bottom = halfSpacing
    }
    outRect.left = 0
    outRect.right = 0
  }

  private fun getPosition(
    parent: RecyclerView,
    view: View
  ): Int {
    // Position omitting without filters view
    return parent.getChildAdapterPosition(view) - 1
  }
}
