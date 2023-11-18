package com.michaldrabik.ui_progress.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal const val GRID_SPAN_SIZE = 2

internal object ProgressLayoutManagerProvider {

  fun provideLayoutManger(context: Context): LayoutManager {
    return if (context.isTablet()) {
      GridLayoutManager(context, GRID_SPAN_SIZE)
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
