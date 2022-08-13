package com.michaldrabik.ui_base.common.sheets.context_menu.show.cases

import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb
import com.michaldrabik.data_local.database.model.Season as SeasonDb

@ViewModelScoped
class ShowContextMenuMyShowsCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun moveToMyShows(traktId: IdTrakt) = coroutineScope {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))

    val (isWatchlist, isHidden) = awaitAll(
      async { showsRepository.watchlistShows.exists(traktId) },
      async { showsRepository.hiddenShows.exists(traktId) }
    )

    val seasons = remoteSource.trakt.fetchSeasons(traktId.id)
      .map { mappers.season.fromNetwork(it) }
      .filter { it.episodes.isNotEmpty() }
      .filter { if (!showSpecials()) !it.isSpecial() else true }

    val episodes = seasons.flatMap { it.episodes }

    transactions.withTransaction {
      val localSeasons = localSource.seasons.getAllByShowId(traktId.id)
      val localEpisodes = localSource.episodes.getAllByShowId(traktId.id)
      val lastWatchedAt = localEpisodes.maxByOrNull { it.lastWatchedAt != null }?.lastWatchedAt?.toMillis() ?: 0L

      showsRepository.myShows.insert(traktId, lastWatchedAt)

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
          episodesToAdd.add(mappers.episode.toDatabase(episode, season, traktId, false, null))
        }
      }

      localSource.seasons.upsert(seasonsToAdd)
      localSource.episodes.upsert(episodesToAdd)
    }

    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()

    RemoveTraktUiEvent(removeWatchlist = isWatchlist, removeHidden = isHidden)
  }

  suspend fun removeFromMyShows(traktId: IdTrakt, removeLocalData: Boolean) {
    val show = Show.EMPTY.copy(ids = Ids.EMPTY.copy(traktId))
    transactions.withTransaction {
      showsRepository.myShows.delete(show.ids.trakt)

      if (removeLocalData) {
        localSource.episodes.deleteAllUnwatchedForShow(show.traktId)
        val seasons = localSource.seasons.getAllByShowId(show.traktId)
        val episodes = localSource.episodes.getAllByShowId(show.traktId)
        val toDelete = mutableListOf<SeasonDb>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        localSource.seasons.delete(toDelete)
      }

      pinnedItemsRepository.removePinnedItem(show)
      announcementManager.refreshShowsAnnouncements()
    }
  }

  private suspend fun showSpecials() =
    settingsRepository.load().specialSeasonsEnabled
}
