package com.michaldrabik.ui_discover.cases

import android.content.Context
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.AppScopeProvider
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_model.DiscoverFilters
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

@ViewModelScoped
class DiscoverFiltersCase @Inject constructor(
  @ApplicationContext private val context: Context,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadFilters(): DiscoverFilters {
    val settings = settingsRepository.load()
    return DiscoverFilters(
      feedOrder = settings.discoverFilterFeed,
      hideAnticipated = !settings.showAnticipatedShows,
      hideCollection = !settings.showCollectionShows,
      genres = settings.discoverFilterGenres.toList(),
      networks = settings.discoverFilterNetworks.toList()
    )
  }

  fun revertFilters(
    initialFilters: DiscoverFilters?,
    currentFilters: DiscoverFilters?,
  ) {
    (context as AppScopeProvider).appScope.launch {
      try {
        if (initialFilters != currentFilters) {
          initialFilters?.let { initial ->
            val settings = settingsRepository.load()
            settingsRepository.update(
              settings.copy(
                discoverFilterFeed = initial.feedOrder,
                discoverFilterGenres = initial.genres,
                discoverFilterNetworks = initial.networks,
                showAnticipatedShows = !initial.hideAnticipated,
                showCollectionShows = !initial.hideCollection
              )
            )
          }
        }
      } catch (error: Throwable) {
        rethrowCancellation(error)
      }
    }
  }

  suspend fun toggleAnticipated() {
    val settings = settingsRepository.load()
    settingsRepository.update(
      settings.copy(showAnticipatedShows = !settings.showAnticipatedShows)
    )
  }

  suspend fun toggleCollection() {
    val settings = settingsRepository.load()
    settingsRepository.update(
      settings.copy(showCollectionShows = !settings.showCollectionShows)
    )
  }
}
