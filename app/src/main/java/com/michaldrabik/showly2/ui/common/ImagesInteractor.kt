package com.michaldrabik.showly2.ui.common

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Image.Status.AVAILABLE
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.UserManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class ImagesInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userManager: UserManager,
  private val mappers: Mappers
) {

  suspend fun findCachedImage(show: Show, type: ImageType): Image {
    val cachedImage = database.imagesDao().getById(show.ids.tvdb, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type)
      else -> mappers.image.fromDatabase(cachedImage).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(show: Show, type: ImageType, force: Boolean = false): Image {
    val tvdbId = show.ids.tvdb
    val cachedImage = findCachedImage(show, type)
    if (cachedImage.status == AVAILABLE && !force) {
      return cachedImage
    }

    checkAuthorization()
    val images = cloud.tvdbApi.fetchImages(userManager.tvdbToken, tvdbId, type.key)
    val remoteImage = images.firstOrNull()

    val image = when (remoteImage) {
      null -> Image.createUnavailable(type)
      else -> Image(cachedImage.id, tvdbId, type, remoteImage.fileName, remoteImage.thumbnail, AVAILABLE)
    }

    when (image.status) {
      UNAVAILABLE -> database.imagesDao().deleteById(tvdbId, image.type.key)
      else -> database.imagesDao().insert(mappers.image.toDatabase(image))
    }

    return image
  }

  private suspend fun checkAuthorization() {
    if (!userManager.isTvdbAuthorized) {
      val token = cloud.tvdbApi.authorize()
      userManager.tvdbToken = token.token
    }
  }
}