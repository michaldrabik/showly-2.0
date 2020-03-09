package com.michaldrabik.showly2.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.showly2.ui.watchlist.WatchlistInteractor
import javax.inject.Inject

class WatchlistWidgetService : RemoteViewsService() {

  @Inject
  lateinit var watchlistInteractor: WatchlistInteractor

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
  }

  override fun onGetViewFactory(intent: Intent?) =
    WatchlistWidgetViewsFactory(applicationContext, watchlistInteractor)
}
