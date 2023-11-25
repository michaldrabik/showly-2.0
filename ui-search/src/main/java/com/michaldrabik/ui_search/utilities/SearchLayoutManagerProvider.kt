package com.michaldrabik.ui_search.utilities

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal object SearchLayoutManagerProvider {

  fun provideLayoutManger(
    context: Context,
    gridSpanSize: Int
  ): RecyclerView.LayoutManager {
    return if (context.isTablet()) {
      GridLayoutManager(context, gridSpanSize)
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
