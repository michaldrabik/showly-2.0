package com.michaldrabik.showly2.common.images

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Image.Status.AVAILABLE
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.ImageFamily.EPISODE
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.UserTvdbManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class EpisodeImagesProvider @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userManager: UserTvdbManager,
  private val mappers: Mappers
) {

  suspend fun findCachedImage(episode: Episode, type: ImageType): Image {
    val cachedImage = database.imagesDao().getByEpisodeId(episode.ids.tvdb.id, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type, EPISODE)
      else -> mappers.image.fromDatabase(cachedImage).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(episode: Episode, force: Boolean = false): Image {
    val tvdbId = episode.ids.tvdb
    val cachedImage = findCachedImage(episode, FANART)
    if (cachedImage.status == AVAILABLE && !force) {
      return cachedImage
    }

    userManager.checkAuthorization()
    var image = Image.createUnavailable(FANART)
    runCatching {
      val remoteImage = cloud.tvdbApi.fetchEpisodeImage(userManager.getToken(), tvdbId.id)
      image = when (remoteImage) {
        null -> Image.createUnavailable(FANART)
        else -> Image(remoteImage.id, tvdbId, FANART, EPISODE, remoteImage.fileName, remoteImage.thumbnail, AVAILABLE)
      }
    }

    when (image.status) {
      UNAVAILABLE -> database.imagesDao().deleteByEpisodeId(tvdbId.id, image.type.key)
      else -> database.imagesDao().insertEpisodeImage(mappers.image.toDatabase(image))
    }

    return image
  }
}
