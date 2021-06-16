package com.michaldrabik.ui_widgets.progress

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_progress.main.cases.ProgressMainLoadItemsCase
import com.michaldrabik.ui_progress.main.cases.ProgressMainSortOrderCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProgressWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressMainLoadItemsCase
  @Inject lateinit var progressSortOrderCase: ProgressMainSortOrderCase
  @Inject lateinit var showsRepository: ShowsRepository
  @Inject lateinit var imagesProvider: ShowImagesProvider
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    ProgressWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      progressSortOrderCase,
      showsRepository,
      imagesProvider,
      settingsRepository
    )
}
