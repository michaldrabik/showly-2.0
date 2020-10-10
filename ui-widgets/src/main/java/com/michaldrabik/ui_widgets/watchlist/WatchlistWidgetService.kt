package com.michaldrabik.ui_widgets.watchlist

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_repository.shows.ShowsRepository
import com.michaldrabik.ui_watchlist.main.cases.WatchlistLoadItemsCase
import com.michaldrabik.ui_watchlist.main.cases.WatchlistSortOrderCase
import com.michaldrabik.ui_widgets.di.UiWidgetsComponentProvider
import javax.inject.Inject

class WatchlistWidgetService : RemoteViewsService() {

  @Inject lateinit var watchlistLoadItemsCase: WatchlistLoadItemsCase
  @Inject lateinit var watchlistSortOrderCase: WatchlistSortOrderCase
  @Inject lateinit var showsRepository: ShowsRepository
  @Inject lateinit var imagesProvider: ShowImagesProvider

  override fun onCreate() {
    super.onCreate()
    (application as UiWidgetsComponentProvider).provideWidgetsComponent().inject(this)
  }

  override fun onGetViewFactory(intent: Intent?) =
    WatchlistWidgetViewsFactory(
      applicationContext,
      watchlistLoadItemsCase,
      watchlistSortOrderCase,
      showsRepository,
      imagesProvider
    )
}
