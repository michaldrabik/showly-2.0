package com.michaldrabik.ui_lists.details.recycler.helpers

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.michaldrabik.ui_lists.details.views.grid.ListDetailsGridItemView
import com.michaldrabik.ui_lists.details.views.grid.ListDetailsGridTitleItemView

class ListDetailsGridItemDecoration : ItemDecoration {

  private var spacing: Int
  private var halfSpacing: Int

  constructor(
    context: Context,
    @DimenRes spacingDimen: Int,
  ) {
    this.spacing = context.resources.getDimensionPixelSize(spacingDimen)
    this.halfSpacing = spacing / 2
  }

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State,
  ) {
    if (parent.layoutManager !is GridLayoutManager) return

    val totalSpan = (parent.layoutManager as GridLayoutManager).spanCount

    if (view is ListDetailsGridItemView || view is ListDetailsGridTitleItemView) {
      outRect.top = halfSpacing
      outRect.bottom = halfSpacing

      val position = parent.getChildAdapterPosition(view)
      val column = position % totalSpan

      outRect.left = spacing * column / totalSpan
      outRect.right = spacing * ((totalSpan - 1) - column) / totalSpan
    }
  }
}
