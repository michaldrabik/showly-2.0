package com.michaldrabik.ui_show.sections.seasons.recycler.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.michaldrabik.repository.settings.SettingsViewModeRepository
import com.michaldrabik.ui_base.utilities.extensions.isTablet

internal object SeasonsLayoutManagerProvider {

  fun provideLayoutManger(
    context: Context,
    settings: SettingsViewModeRepository,
  ): RecyclerView.LayoutManager {
    return if (context.isTablet()) {
      GridLayoutManager(context, settings.tabletGridSpanSize)
    } else {
      LinearLayoutManager(context, VERTICAL, false)
    }
  }
}
