package com.michaldrabik.ui_show.cases

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb
import com.michaldrabik.data_local.database.model.Season as SeasonDb

@ViewModelScoped
class ShowDetailsMyShowsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val announcementManager: AnnouncementManager
) {

  suspend fun getAllIds() = coroutineScope {
    val (myShows, watchlistShows) = awaitAll(
      async { showsRepository.myShows.loadAllIds() },
      async { showsRepository.watchlistShows.loadAllIds() }
    )
    Pair(myShows, watchlistShows)
  }

  suspend fun isMyShows(show: Show) =
    showsRepository.myShows.load(show.ids.trakt) != null

  suspend fun addToMyShows(
    show: Show,
    seasons: List<Season>,
    episodes: List<Episode>
  ) {
    database.withTransaction {
      showsRepository.myShows.insert(show.ids.trakt)

      val localSeasons = database.seasonsDao().getAllByShowId(show.traktId)
      val localEpisodes = database.episodesDao().getAllByShowId(show.traktId)

      val seasonsToAdd = mutableListOf<SeasonDb>()
      val episodesToAdd = mutableListOf<EpisodeDb>()

      seasons.forEach { season ->
        if (localSeasons.none { it.idTrakt == season.ids.trakt.id }) {
          seasonsToAdd.add(mappers.season.toDatabase(season, show.ids.trakt, false))
        }
      }
      episodes.forEach { episode ->
        if (localEpisodes.none { it.idTrakt == episode.ids.trakt.id }) {
          val season = seasons.find { it.number == episode.season }!!
          episodesToAdd.add(mappers.episode.toDatabase(episode, season, show.ids.trakt, false))
        }
      }

      database.seasonsDao().upsert(seasonsToAdd)
      database.episodesDao().upsert(episodesToAdd)
    }

    pinnedItemsRepository.removePinnedItem(show)
    announcementManager.refreshShowsAnnouncements()
  }

  suspend fun removeFromMyShows(show: Show, removeLocalData: Boolean) {
    database.withTransaction {
      showsRepository.myShows.delete(show.ids.trakt)

      if (removeLocalData) {
        database.episodesDao().deleteAllUnwatchedForShow(show.traktId)
        val seasons = database.seasonsDao().getAllByShowId(show.traktId)
        val episodes = database.episodesDao().getAllByShowId(show.traktId)
        val toDelete = mutableListOf<SeasonDb>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        database.seasonsDao().delete(toDelete)
      }

      pinnedItemsRepository.removePinnedItem(show)
    }
  }
}
