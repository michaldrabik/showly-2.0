package com.michaldrabik.showly2.ui.show

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config.ACTORS_CACHE_DURATION
import com.michaldrabik.showly2.UserManager
import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Episode as EpisodeDb
import com.michaldrabik.storage.database.model.Season as SeasonDb

@AppScope
class ShowDetailsInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userManager: UserManager,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository
) {

  suspend fun loadShowDetails(idTrakt: IdTrakt) =
    showsRepository.detailsShow.load(idTrakt)

  suspend fun loadBackgroundImage(show: Show) =
    imagesManager.loadRemoteImage(show, FANART)

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(traktId.id) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadActors(show: Show): List<Actor> {
    val localActors = database.actorsDao().getAllByShow(show.ids.tvdb.id)
    if (localActors.isNotEmpty() && nowUtcMillis() - localActors[0].updatedAt < ACTORS_CACHE_DURATION) {
      return localActors
        .sortedWith(compareBy({ it.image.isBlank() }, { it.sortOrder }))
        .map { mappers.actor.fromDatabase(it) }
    }

    userManager.checkAuthorization()
    val token = userManager.getTvdbToken()
    val remoteActors = cloud.tvdbApi.fetchActors(token, show.ids.tvdb.id)
      .sortedWith(compareBy({ it.image.isBlank() }, { it.sortOrder }))
      .take(20)
      .map { mappers.actor.fromNetwork(it) }

    database.actorsDao().replace(remoteActors.map { mappers.actor.toDatabase(it) }, show.ids.tvdb.id)
    return remoteActors
  }

  suspend fun loadRelatedShows(show: Show): List<Show> =
    showsRepository.relatedShows.loadAll(show)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)

  suspend fun loadSeasons(show: Show): List<Season> {
    return cloud.traktApi.fetchSeasons(show.ids.trakt.id).asSequence()
      .map { mappers.season.fromNetwork(it) }
      .toList()
  }

  suspend fun isFollowed(show: Show) =
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

  suspend fun removeFromFollowed(show: Show) {
    database.withTransaction {
      showsRepository.myShows.delete(show.ids.trakt)
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
  }

  suspend fun isWatchLater(show: Show) =
    showsRepository.seeLaterShows.load(show.ids.trakt) != null

  suspend fun addToWatchLater(show: Show) =
    showsRepository.seeLaterShows.insert(show.ids.trakt)

  suspend fun removeFromWatchLater(show: Show) =
    showsRepository.seeLaterShows.delete(show.ids.trakt)
}