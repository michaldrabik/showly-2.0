package com.michaldrabik.ui_search.utilities

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.utilities.extensions.isTablet

private const val GRID_SPAN_SIZE = 2

internal object SearchLayoutManagerProvider {

  fun provideLayoutManger(context: Context): RecyclerView.LayoutManager {
    return if (context.isTablet()) {
      GridLayoutManager(context, GRID_SPAN_SIZE)
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
