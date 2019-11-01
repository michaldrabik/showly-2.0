package com.michaldrabik.showly2.ui.common

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated episodes data for current watchlist items.
 */
@AppScope
class EpisodesSynchronizer @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  fun synchronize() {

  }
}