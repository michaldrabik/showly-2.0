package com.michaldrabik.showly2.ui.main

import androidx.room.withTransaction
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Settings
import javax.inject.Inject

@AppScope
class MainInteractor @Inject constructor(
  private val database: AppDatabase,
  private val uiCache: UiCache
) {

  suspend fun initSettings() {
    val settings = database.settingsDao().getAll()
    if (settings == null) {
      val newSettings = Settings(
        isInitialRun = true,
        myShowsEndedSortBy = SortOrder.NAME.name,
        myShowsIncomingSortBy = SortOrder.NAME.name,
        myShowsRunningSortBy = SortOrder.NAME.name
      )
      database.settingsDao().upsert(newSettings)
    }
  }

  suspend fun setInitialRun(value: Boolean) {
    database.withTransaction {
      val settings = database.settingsDao().getAll()
      settings?.let {
        database.settingsDao().upsert(it.copy(isInitialRun = value))
      }
    }
  }

  suspend fun isInitialRun(): Boolean {
    val settings = database.settingsDao().getAll()
    return settings?.isInitialRun ?: true
  }

  fun clearCache() {
    uiCache.clear()
  }
}