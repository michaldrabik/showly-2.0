package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb
import com.michaldrabik.data_local.database.model.Season as SeasonDb

@ViewModelScoped
class ShowContextMenuMyShowsCase @Inject constructor(
  private val database: AppDatabase,
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun moveToMyShows(traktId: IdTrakt) {
    val seasons = cloud.traktApi.fetchSeasons(traktId.id)
      .map { mappers.season.fromNetwork(it) }
      .filter { it.episodes.isNotEmpty() }
      .filter { if (!showSpecials()) !it.isSpecial() else true }

    val episodes = seasons.flatMap { it.episodes }

    database.runTransaction {
      with(showsRepository) {
        myShows.insert(traktId)
        watchlistShows.delete(traktId)
        hiddenShows.delete(traktId)
      }

      val localSeasons = database.seasonsDao().getAllByShowId(traktId.id)
      val localEpisodes = database.episodesDao().getAllByShowId(traktId.id)

      val seasonsToAdd = mutableListOf<SeasonDb>()
      val episodesToAdd = mutableListOf<EpisodeDb>()

      seasons.forEach { season ->
        if (localSeasons.none { it.idTrakt == season.ids.trakt.id }) {
          seasonsToAdd.add(mappers.season.toDatabase(season, traktId, false))
        }
      }
      episodes.forEach { episode ->
        if (localEpisodes.none { it.idTrakt == episode.ids.trakt.id }) {
          val season = seasons.find { it.number == episode.season }!!
          episodesToAdd.add(mappers.episode.toDatabase(episode, season, traktId, false))
        }
      }

      database.seasonsDao().upsert(seasonsToAdd)
      database.episodesDao().upsert(episodesToAdd)
    }

    announcementManager.refreshShowsAnnouncements()
  }

  suspend fun removeFromMyShows(traktId: IdTrakt, removeLocalData: Boolean) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    database.runTransaction {
      showsRepository.myShows.delete(show.ids.trakt)

      if (removeLocalData) {
        episodesDao().deleteAllUnwatchedForShow(show.traktId)
        val seasons = seasonsDao().getAllByShowId(show.traktId)
        val episodes = episodesDao().getAllByShowId(show.traktId)
        val toDelete = mutableListOf<SeasonDb>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        seasonsDao().delete(toDelete)
      }

      pinnedItemsRepository.removePinnedItem(show)
    }
  }

  private suspend fun showSpecials() =
    settingsRepository.load().specialSeasonsEnabled
}
