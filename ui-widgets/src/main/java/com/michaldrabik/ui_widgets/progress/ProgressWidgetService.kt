package com.michaldrabik.ui_widgets.progress

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_progress.progress.cases.ProgressItemsCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProgressWidgetService : RemoteViewsService() {

  @Inject lateinit var progressItemsCase: ProgressItemsCase
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onGetViewFactory(intent: Intent?) =
    ProgressWidgetViewsFactory(
      applicationContext,
      progressItemsCase,
      settingsRepository
    )
}
