package com.michaldrabik.ui_news.providers

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.michaldrabik.repository.settings.SettingsViewModeRepository
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal object NewsLayoutManagerProvider {

  fun provideLayoutManger(
    context: Context,
    settings: SettingsViewModeRepository,
  ): LayoutManager {
    return if (context.isTablet()) {
      StaggeredGridLayoutManager(settings.tabletGridSpanSize, VERTICAL).apply {
        gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
      }
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
