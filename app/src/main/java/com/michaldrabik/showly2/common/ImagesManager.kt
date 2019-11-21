package com.michaldrabik.showly2.common

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Image.Status.AVAILABLE
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.ImageFamily.EPISODE
import com.michaldrabik.showly2.model.ImageFamily.SHOW
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class ImagesManager @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userManager: UserManager,
  private val mappers: Mappers
) {

  suspend fun findCachedImage(show: Show, type: ImageType): Image {
    val cachedImage = database.imagesDao().getByShowId(show.ids.tvdb.id, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type, SHOW)
      else -> mappers.image.fromDatabase(cachedImage).copy(type = type)
    }
  }

  suspend fun findCachedImage(episode: Episode, type: ImageType): Image {
    val cachedImage = database.imagesDao().getByEpisodeId(episode.ids.tvdb.id, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type, EPISODE)
      else -> mappers.image.fromDatabase(cachedImage).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(show: Show, type: ImageType, force: Boolean = false): Image {
    val tvdbId = show.ids.tvdb.id
    val cachedImage = findCachedImage(show, type)
    if (cachedImage.status == AVAILABLE && !force) {
      return cachedImage
    }

    userManager.checkAuthorization()
    val images = cloud.tvdbApi.fetchShowImages(userManager.getTvdbToken(), tvdbId)

    var typeImages = images.filter { it.keyType == type.key }
    //If requested poster is unavailable try backing up to a fanart
    if (typeImages.isEmpty() && type == POSTER) {
      typeImages = images.filter { it.keyType == FANART.key }
    }

    val remoteImage = typeImages.maxBy { it.rating.count }
    val image = when (remoteImage) {
      null -> Image.createUnavailable(type)
      else -> Image(remoteImage.id, tvdbId, type, SHOW, remoteImage.fileName, remoteImage.thumbnail, AVAILABLE)
    }

    when (image.status) {
      UNAVAILABLE -> database.imagesDao().deleteByShowId(tvdbId, image.type.key)
      else -> database.imagesDao().insertShowImage(mappers.image.toDatabase(image))
    }

    return image
  }

  suspend fun loadRemoteImage(episode: Episode, force: Boolean = false): Image {
    val tvdbId = episode.ids.tvdb.id
    val cachedImage = findCachedImage(episode, FANART)
    if (cachedImage.status == AVAILABLE && !force) {
      return cachedImage
    }

    userManager.checkAuthorization()
    val remoteImage = cloud.tvdbApi.fetchEpisodeImage(userManager.getTvdbToken(), tvdbId)

    val image = when (remoteImage) {
      null -> Image.createUnavailable(FANART)
      else -> Image(remoteImage.id, tvdbId, FANART, EPISODE, remoteImage.fileName, remoteImage.thumbnail, AVAILABLE)
    }

    when (image.status) {
      UNAVAILABLE -> database.imagesDao().deleteByEpisodeId(tvdbId, image.type.key)
      else -> database.imagesDao().insertEpisodeImage(mappers.image.toDatabase(image))
    }

    return image
  }

  suspend fun loadRemoteImages(show: Show, type: ImageType): List<Image> {
    val tvdbId = show.ids.tvdb.id

    userManager.checkAuthorization()
    val remoteImages = cloud.tvdbApi.fetchShowImages(userManager.getTvdbToken(), tvdbId)

    return remoteImages
      .filter { it.keyType == type.key }
      .map {
        Image(it.id, tvdbId, type, SHOW, it.fileName, it.thumbnail, AVAILABLE)
      }
  }

  suspend fun checkAuthorization() = userManager.checkAuthorization()
}