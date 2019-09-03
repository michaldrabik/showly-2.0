package com.michaldrabik.showly2.ui.shows

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesInteractor
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class ShowDetailsInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesInteractor: ImagesInteractor,
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
    imagesInteractor.loadRemoteImage(show, FANART)

  suspend fun loadNextEpisode(show: Show): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(show.ids.trakt) ?: return null
    return mappers.episode.fromNetwork(episode)
  }
}