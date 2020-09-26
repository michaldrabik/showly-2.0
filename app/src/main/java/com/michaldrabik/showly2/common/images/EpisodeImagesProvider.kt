package com.michaldrabik.showly2.common.images

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Image.Status.AVAILABLE
import com.michaldrabik.ui_model.Image.Status.UNAVAILABLE
import com.michaldrabik.ui_model.ImageFamily.EPISODE
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class EpisodeImagesProvider @Inject constructor(
    private val cloud: Cloud,
    private val database: AppDatabase,
    private val userManager: UserTvdbManager,
    private val mappers: Mappers
) {

  private suspend fun findCachedImage(episode: Episode, type: ImageType): Image {
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
        else -> Image(remoteImage.id ?: -1, tvdbId, FANART, EPISODE, remoteImage.fileName ?: "", remoteImage.thumbnail ?: "", AVAILABLE)
      }
    }

    when (image.status) {
      UNAVAILABLE -> database.imagesDao().deleteByEpisodeId(tvdbId.id, image.type.key)
      else -> database.imagesDao().insertEpisodeImage(mappers.image.toDatabase(image))
    }

    return image
  }
}
