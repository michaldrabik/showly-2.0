package com.michaldrabik.showly2.ui.show.cases

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.PinnedItemsRepository
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Episode as EpisodeDb
import com.michaldrabik.storage.database.model.Season as SeasonDb

@AppScope
class ShowDetailsFollowedCase @Inject constructor(
  private val database: AppDatabase,
  private val cloud: Cloud,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val episodesManager: EpisodesManager,
  private val userManager: UserTraktManager
) {

  suspend fun isMyShows(show: Show) =
    showsRepository.myShows.load(show.ids.trakt) != null

  suspend fun addToFollowed(
    show: Show,
    seasons: List<Season>,
    episodes: List<Episode>
  ) {
    database.withTransaction {
      showsRepository.myShows.insert(show.ids.trakt)

      val localSeasons = database.seasonsDao().getAllByShowId(show.ids.trakt.id)
      val localEpisodes = database.episodesDao().getAllForShows(listOf(show.ids.trakt.id))

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
  }

  suspend fun removeFromFollowed(show: Show, removeLocalData: Boolean) {
    database.withTransaction {
      showsRepository.myShows.delete(show.ids.trakt)

      if (removeLocalData) {
        database.episodesDao().deleteAllUnwatchedForShow(show.ids.trakt.id)
        val seasons = database.seasonsDao().getAllByShowId(show.ids.trakt.id)
        val episodes = database.episodesDao().getAllForShows(listOf(show.ids.trakt.id))
        val toDelete = mutableListOf<SeasonDb>()
        seasons.forEach { season ->
          if (episodes.none { it.idSeason == season.idTrakt }) {
            toDelete.add(season)
          }
        }
        database.seasonsDao().delete(toDelete)
      }

      pinnedItemsRepository.removePinnedItem(show.traktId)
    }
  }

  suspend fun removeTraktHistory(show: Show) {
    val token = userManager.checkAuthorization()
    val request = SyncExportRequest(listOf(SyncExportItem.create(show.traktId)))
    cloud.traktApi.postDeleteProgress(token.token, request)
    episodesManager.setAllUnwatched(show)
  }
}
