package com.michaldrabik.showly2.ui.show

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.UserManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.*
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

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
    if (localShow == null) {
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
    userManager.checkAuthorization()
    val token = userManager.getTvdbToken()
    return cloud.tvdbApi.fetchActors(token, show.ids.tvdb)
      .filter { it.image.isNotBlank() }
      .sortedBy { it.sortOrder }
      .take(30)
      .map { mappers.actor.fromNetwork(it) }
  }

  suspend fun loadRelatedShows(show: Show) =
    cloud.traktApi.fetchRelatedShows(show.ids.trakt)
      .sortedWith(compareBy({ it.votes }, { it.rating }))
      .reversed()
      .map { mappers.show.fromNetwork(it) }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)

  suspend fun loadSeasons(show: Show): List<Season> {
    return cloud.traktApi.fetchSeasons(show.ids.trakt)
      .filter { it.number != 0 } //Filtering out "special" seasons
      .map { mappers.season.fromNetwork(it) }
  }
}