package com.michaldrabik.ui_my_movies.mymovies.utilities

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.michaldrabik.ui_base.utilities.extensions.isTablet
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesAdapter
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieAllCompactView
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieAllView

class MyMoviesListItemDecoration : ItemDecoration {

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
    if (view !is MyMovieAllView && view !is MyMovieAllCompactView) {
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
    if (view is MyMovieAllView) {
      outRect.top = spacing
      outRect.bottom = spacing
    } else if (view is MyMovieAllCompactView) {
      outRect.top = halfSpacing
      outRect.bottom = halfSpacing
    }

    val totalSpan = (parent.layoutManager as GridLayoutManager).spanCount
    val column = getPosition(parent, view) % totalSpan

    outRect.left = (spacing * 2) * column / totalSpan
    outRect.right = (spacing * 2) * ((totalSpan - 1) - column) / totalSpan
  }

  private fun getItemOffsetsPhone(outRect: Rect, view: View) {
    if (view is MyMovieAllView) {
      outRect.top = spacing
      outRect.bottom = spacing
    } else if (view is MyMovieAllCompactView) {
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
    val nonMyShowItemCount = (parent.adapter as MyMoviesAdapter)
      .getItems()
      .count { it.type != Type.ALL_MOVIES_ITEM }

    return parent.getChildAdapterPosition(view) - nonMyShowItemCount
  }
}
