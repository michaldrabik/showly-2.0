package com.michaldrabik.ui_show.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.episodes.EpisodesManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.helpers.SeasonsBundle
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsEpisodesCase @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val episodesManager: EpisodesManager
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(traktId.id) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadSeasons(show: Show, isOnline: Boolean): SeasonsBundle {
    try {
      if (!isOnline) throw Error("App is not online. Should fall back to local data.")

      val remoteSeasons = cloud.traktApi.fetchSeasons(show.traktId)
        .map { mappers.season.fromNetwork(it) }
        .filter { it.episodes.isNotEmpty() }
        .filter { if (!showSpecials()) !it.isSpecial() else true }

      val isFollowed = showsRepository.myShows.load(show.ids.trakt) != null
      if (isFollowed) {
        episodesManager.invalidateSeasons(show, remoteSeasons)
      }

      return SeasonsBundle(remoteSeasons, isLocal = false)
    } catch (error: Throwable) {
      Timber.d("Falling back to local data. Error: ${error.message}")

      val localEpisodes = database.episodesDao().getAllByShowId(show.traktId)
      val localSeasons = database.seasonsDao().getAllByShowId(show.traktId).map { season ->
        val seasonEpisodes = localEpisodes.filter { ep -> ep.idSeason == season.idTrakt }
        mappers.season.fromDatabase(season, seasonEpisodes)
      }
        .filter { it.episodes.isNotEmpty() }
        .filter { if (!showSpecials()) !it.isSpecial() else true }

      return SeasonsBundle(localSeasons, isLocal = true)
    }
  }

  private suspend fun showSpecials() =
    settingsRepository.load().specialSeasonsEnabled
}
