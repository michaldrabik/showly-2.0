package com.michaldrabik.ui_my_movies.common.layout

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieGridTitleView
import com.michaldrabik.ui_my_movies.common.views.CollectionMovieGridView

class CollectionMovieGridItemDecoration : ItemDecoration {

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

    if (view is CollectionMovieGridView || view is CollectionMovieGridTitleView) {
      outRect.top = halfSpacing
      outRect.bottom = halfSpacing

      val position = parent.getChildAdapterPosition(view) - 1
      val column = position % totalSpan

      outRect.left = spacing * column / totalSpan
      outRect.right = spacing * ((totalSpan - 1) - column) / totalSpan
    }
  }
}
