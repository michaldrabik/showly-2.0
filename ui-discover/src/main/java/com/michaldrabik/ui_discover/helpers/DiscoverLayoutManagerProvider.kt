package com.michaldrabik.ui_discover.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import com.michaldrabik.common.Config.MAIN_GRID_SPAN
import com.michaldrabik.common.Config.MAIN_GRID_SPAN_TABLET
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal object DiscoverLayoutManagerProvider {

  fun provideLayoutManager(context: Context): GridLayoutManager {
    val span = if (context.isTablet()) MAIN_GRID_SPAN_TABLET else MAIN_GRID_SPAN
    return GridLayoutManager(context, span)
  }
}
