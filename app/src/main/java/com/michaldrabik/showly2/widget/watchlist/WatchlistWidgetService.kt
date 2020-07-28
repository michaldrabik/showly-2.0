package com.michaldrabik.showly2.widget.watchlist

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.WatchlistInteractor
import javax.inject.Inject

class WatchlistWidgetService : RemoteViewsService() {

  @Inject lateinit var watchlistInteractor: WatchlistInteractor
  @Inject lateinit var showsRepository: ShowsRepository

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
  }

  override fun onGetViewFactory(intent: Intent?) =
    WatchlistWidgetViewsFactory(applicationContext, watchlistInteractor, showsRepository)
}
