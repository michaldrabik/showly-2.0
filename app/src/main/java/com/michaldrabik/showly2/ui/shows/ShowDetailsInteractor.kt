package com.michaldrabik.showly2.ui.shows

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.UserManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.Show
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

  suspend fun loadNextEpisode(show: Show): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(show.ids.trakt) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadActors(show: Show): List<Actor> {
    userManager.checkAuthorization()
    val token = userManager.getTvdbToken()
    return cloud.tvdbApi.fetchActors(token, show.ids.tvdb)
      .filter { it.image.isNotBlank() }
      .sortedBy { it.sortOrder }
      .take(15)
      .map { mappers.actor.fromNetwork(it) }
  }
}