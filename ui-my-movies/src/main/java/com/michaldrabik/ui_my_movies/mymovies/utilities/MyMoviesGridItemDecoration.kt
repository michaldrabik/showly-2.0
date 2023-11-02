package com.michaldrabik.ui_my_movies.mymovies.utilities

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesAdapter
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieAllGridTitleView
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieAllGridView

class MyMoviesGridItemDecoration : ItemDecoration {

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

    if (view is MyMovieAllGridView || view is MyMovieAllGridTitleView) {
      outRect.top = halfSpacing
      outRect.bottom = halfSpacing

      val nonMyShowItemCount = (parent.adapter as MyMoviesAdapter)
        .getItems()
        .count { it.type != Type.ALL_MOVIES_ITEM }

      val position = parent.getChildAdapterPosition(view) - nonMyShowItemCount
      val column = position % totalSpan

      outRect.left = spacing * column / totalSpan
      outRect.right = spacing * ((totalSpan - 1) - column) / totalSpan
    }
  }
}
