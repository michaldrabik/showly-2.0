package com.michaldrabik.ui_lists.details.recycler.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.common.Config.LISTS_GRID_SPAN
import com.michaldrabik.common.Config.LISTS_GRID_SPAN_TABLET
import com.michaldrabik.common.Config.LISTS_STANDARD_GRID_SPAN_TABLET
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal object ListDetailsLayoutManagerProvider {

  fun provideLayoutManger(context: Context, viewMode: ListViewMode): RecyclerView.LayoutManager {
    return if (context.isTablet()) {
      provideTabletLayout(context, viewMode)
    } else {
      providePhoneLayout(context, viewMode)
    }
  }

  private fun provideTabletLayout(
    context: Context,
    viewMode: ListViewMode,
  ): RecyclerView.LayoutManager {
    return when (viewMode) {
      LIST_NORMAL, LIST_COMPACT -> GridLayoutManager(context, LISTS_STANDARD_GRID_SPAN_TABLET)
      GRID, GRID_TITLE -> GridLayoutManager(context, LISTS_GRID_SPAN_TABLET)
    }
  }

  private fun providePhoneLayout(
    context: Context,
    viewMode: ListViewMode,
  ): RecyclerView.LayoutManager {
    return when (viewMode) {
      LIST_NORMAL, LIST_COMPACT -> LinearLayoutManager(context, VERTICAL, false)
      GRID, GRID_TITLE -> GridLayoutManager(context, LISTS_GRID_SPAN)
    }
  }
}
