package com.michaldrabik.ui_widgets.progress

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_progress.main.cases.ProgressLoadItemsCase
import com.michaldrabik.ui_progress.main.cases.ProgressSortOrderCase
import com.michaldrabik.ui_repository.shows.ShowsRepository
import com.michaldrabik.ui_widgets.di.UiWidgetsComponentProvider
import javax.inject.Inject

class ProgressWidgetService : RemoteViewsService() {

  @Inject lateinit var progressLoadItemsCase: ProgressLoadItemsCase
  @Inject lateinit var progressSortOrderCase: ProgressSortOrderCase
  @Inject lateinit var showsRepository: ShowsRepository
  @Inject lateinit var imagesProvider: ShowImagesProvider

  override fun onCreate() {
    super.onCreate()
    (application as UiWidgetsComponentProvider).provideWidgetsComponent().inject(this)
  }

  override fun onGetViewFactory(intent: Intent?) =
    ProgressWidgetViewsFactory(
      applicationContext,
      progressLoadItemsCase,
      progressSortOrderCase,
      showsRepository,
      imagesProvider
    )
}
