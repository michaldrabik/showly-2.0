package com.michaldrabik.showly2.widget.watchlist

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.showly2.ui.watchlist.cases.WatchlistLoadItemsCase
import com.michaldrabik.showly2.ui.watchlist.cases.WatchlistSortOrderCase
import javax.inject.Inject

class WatchlistWidgetService : RemoteViewsService() {

  @Inject lateinit var watchlistLoadItemsCase: WatchlistLoadItemsCase
  @Inject lateinit var watchlistSortOrderCase: WatchlistSortOrderCase
  @Inject lateinit var showsRepository: ShowsRepository
  @Inject lateinit var imagesProvider: ShowImagesProvider

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
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
