package com.michaldrabik.ui_news.providers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.michaldrabik.ui_base.utilities.extensions.isTablet

private const val GRID_SPAN_SIZE = 2

internal object NewsLayoutManagerProvider {

  fun provideLayoutManger(context: Context): LayoutManager {
    return if (context.isTablet()) {
      StaggeredGridLayoutManager(GRID_SPAN_SIZE, VERTICAL).apply {
        gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
      }
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
