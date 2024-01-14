package com.michaldrabik.ui_progress_movies.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal object ProgressMoviesLayoutManagerProvider {

  fun provideLayoutManger(
    context: Context,
    gridSpanSize: Int,
  ): LayoutManager {
    return if (context.isTablet()) {
      GridLayoutManager(context, gridSpanSize)
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
