package com.michaldrabik.showly2.ui.show

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.Config.ACTORS_CACHE_DURATION
import com.michaldrabik.showly2.Config.RELATED_CACHE_DURATION
import com.michaldrabik.showly2.UserManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.*
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.FollowedShow
import com.michaldrabik.storage.database.model.RelatedShow
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Episode as EpisodeDb
import com.michaldrabik.storage.database.model.Season as SeasonDb

@AppScope
class ShowDetailsInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userManager: UserManager,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadShowDetails(traktId: Long): Show {
    val localShow = database.showsDao().getById(traktId)
    if (localShow == null || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = cloud.traktApi.fetchShow(traktId)
      val show = mappers.show.fromNetwork(remoteShow)
      database.showsDao().upsert(listOf(mappers.show.toDatabase(show)))
      return show
    }
    return mappers.show.fromDatabase(localShow)
  }

  suspend fun loadBackgroundImage(show: Show) =
    imagesManager.loadRemoteImage(show, FANART)

  suspend fun loadNextEpisode(traktId: Long): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(traktId) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadActors(show: Show): List<Actor> {
    val localActors = database.actorsDao().getAllByShow(show.ids.tvdb)
    if (localActors.isNotEmpty() && nowUtcMillis() - localActors[0].updatedAt < ACTORS_CACHE_DURATION) {
      return localActors.map { mappers.actor.fromDatabase(it) }
    }

    userManager.checkAuthorization()
    val token = userManager.getTvdbToken()
    val remoteActors = cloud.tvdbApi.fetchActors(token, show.ids.tvdb)
      .filter { it.image.isNotBlank() }
      .sortedBy { it.sortOrder }
      .take(20)
      .map { mappers.actor.fromNetwork(it) }

    database.actorsDao().deleteAllAndInsert(remoteActors.map { mappers.actor.toDatabase(it) }, show.ids.tvdb)
    return remoteActors
  }

  suspend fun loadRelatedShows(show: Show): List<Show> {
    val relatedShows = database.relatedShowsDao().getById(show.id)
    val latest = relatedShows.maxBy { it.updatedAt }
    if (latest != null && nowUtcMillis() - latest.updatedAt < RELATED_CACHE_DURATION) {
      return relatedShows
        .map { mappers.show.fromDatabase(it) }
        .sortedWith(compareBy({ it.votes }, { it.rating }))
        .reversed()
    }

    val remoteShows = cloud.traktApi.fetchRelatedShows(show.id)
      .map { mappers.show.fromNetwork(it) }

    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.showsDao().upsert(remoteShows.map { mappers.show.toDatabase(it) })
      database.relatedShowsDao().deleteById(show.id)
      database.relatedShowsDao().insert(remoteShows.map { RelatedShow.fromTraktId(it.id, show.id, timestamp) })
    }

    return remoteShows
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)

  suspend fun loadSeasons(show: Show): List<Season> {
    return cloud.traktApi.fetchSeasons(show.id)
      .filter { it.number != 0 } //Filtering out "special" seasons
      .map { mappers.season.fromNetwork(it) }
  }

  suspend fun isFollowed(show: Show) =
    database.followedShowsDao().getById(show.id) != null

  suspend fun addToFollowed(
    show: Show,
    seasons: List<Season>,
    episodes: List<Episode>
  ) {
    val dbShow = FollowedShow.fromTraktId(show.id, nowUtcMillis())
    database.withTransaction {
      database.followedShowsDao().insert(dbShow)

      val localSeasons = database.seasonsDao().getAllByShowId(show.id)
      val localEpisodes = database.episodesDao().getAllByShowId(show.id)

      val seasonsToAdd = mutableListOf<SeasonDb>()
      val episodesToAdd = mutableListOf<EpisodeDb>()

      seasons.forEach { season ->
        if (localSeasons.none { it.idTrakt == season.id }) {
          seasonsToAdd.add(mappers.season.toDatabase(season, show.id, false))
        }
      }

      episodes.forEach { episode ->
        if (localEpisodes.none { it.idTrakt == episode.id }) {
          val season = seasons.find { it.number == episode.season }!!
          episodesToAdd.add(mappers.episode.toDatabase(episode, season, show.id, false))
        }
      }

      database.seasonsDao().upsert(seasonsToAdd)
      database.episodesDao().upsert(episodesToAdd)
    }
  }

  suspend fun removeFromFollowed(show: Show) {
    database.withTransaction {
      database.followedShowsDao().deleteById(show.id)
      database.episodesDao().deleteAllUnwatchedForShow(show.id)

      val seasons = database.seasonsDao().getAllByShowId(show.id)
      val episodes = database.episodesDao().getAllByShowId(show.id)

      val toDelete = mutableListOf<SeasonDb>()
      seasons.forEach { season ->
        if (episodes.none { it.idSeason == season.idTrakt }) {
          toDelete.add(season)
        }
      }
      database.seasonsDao().delete(toDelete)
    }
  }
}