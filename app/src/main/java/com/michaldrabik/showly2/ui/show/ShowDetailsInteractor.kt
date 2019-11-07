package com.michaldrabik.showly2.ui.show

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.Config.ACTORS_CACHE_DURATION
import com.michaldrabik.showly2.Config.RELATED_CACHE_DURATION
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
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.FollowedShow
import com.michaldrabik.storage.database.model.RelatedShow
import com.michaldrabik.storage.database.model.SeeLaterShow
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

  suspend fun loadShowDetails(traktId: IdTrakt): Show {
    val localShow = database.showsDao().getById(traktId.id)
    if (localShow == null || nowUtcMillis() - localShow.updatedAt > Config.SHOW_DETAILS_CACHE_DURATION) {
      val remoteShow = cloud.traktApi.fetchShow(traktId.id)
      val show = mappers.show.fromNetwork(remoteShow)
      database.showsDao().upsert(listOf(mappers.show.toDatabase(show)))
      return show
    }
    return mappers.show.fromDatabase(localShow)
  }

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

    database.actorsDao().deleteAllAndInsert(remoteActors.map { mappers.actor.toDatabase(it) }, show.ids.tvdb.id)
    return remoteActors
  }

  suspend fun loadRelatedShows(show: Show): List<Show> {
    val relatedShows = database.relatedShowsDao().getAllById(show.ids.trakt.id)
    val latest = relatedShows.maxBy { it.updatedAt }
    if (latest != null && nowUtcMillis() - latest.updatedAt < RELATED_CACHE_DURATION) {
      val relatedShowsIds = relatedShows.map { it.idTrakt }
      return database.showsDao().getAll(relatedShowsIds)
        .map { mappers.show.fromDatabase(it) }
        .sortedWith(compareBy({ it.votes }, { it.rating }))
        .reversed()
    }

    val remoteShows = cloud.traktApi.fetchRelatedShows(show.ids.trakt.id)
      .map { mappers.show.fromNetwork(it) }

    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.showsDao().upsert(remoteShows.map { mappers.show.toDatabase(it) })
      database.relatedShowsDao().deleteById(show.ids.trakt.id)
      database.relatedShowsDao().insert(remoteShows.map { RelatedShow.fromTraktId(it.ids.trakt.id, show.ids.trakt.id, timestamp) })
    }

    return remoteShows
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)

  suspend fun loadSeasons(show: Show) =
    cloud.traktApi.fetchSeasons(show.ids.trakt.id).asSequence()
      .map { mappers.season.fromNetwork(it) }
      .toList()

  suspend fun isFollowed(show: Show) =
    database.followedShowsDao().getById(show.ids.trakt.id) != null

  suspend fun addToFollowed(
    show: Show,
    seasons: List<Season>,
    episodes: List<Episode>
  ) {
    val dbShow = FollowedShow.fromTraktId(show.ids.trakt.id, nowUtcMillis())
    database.withTransaction {
      database.followedShowsDao().insert(dbShow)

      val localSeasons = database.seasonsDao().getAllByShowId(show.ids.trakt.id)
      val localEpisodes = database.episodesDao().getAllByShowId(show.ids.trakt.id)

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
      database.followedShowsDao().deleteById(show.ids.trakt.id)
      database.episodesDao().deleteAllUnwatchedForShow(show.ids.trakt.id)

      val seasons = database.seasonsDao().getAllByShowId(show.ids.trakt.id)
      val episodes = database.episodesDao().getAllByShowId(show.ids.trakt.id)

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
    database.seeLaterShowsDao().getById(show.ids.trakt.id) != null

  suspend fun addToWatchLater(show: Show) {
    val dbShow = SeeLaterShow.fromTraktId(show.ids.trakt.id, nowUtcMillis())
    database.seeLaterShowsDao().insert(dbShow)
  }

  suspend fun removeFromWatchLater(show: Show) {
    database.seeLaterShowsDao().deleteById(show.ids.trakt.id)
  }
}