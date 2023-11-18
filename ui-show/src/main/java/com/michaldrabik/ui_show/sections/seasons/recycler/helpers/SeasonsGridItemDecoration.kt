package com.michaldrabik.ui_show.sections.seasons.recycler.helpers

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SeasonsGridItemDecoration : ItemDecoration {

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
    val totalSpan = (parent.layoutManager as GridLayoutManager).spanCount

    val position = parent.getChildAdapterPosition(view)
    val column = position % totalSpan

    outRect.left = spacing * column / totalSpan
    outRect.right = spacing * ((totalSpan - 1) - column) / totalSpan
  }
}
