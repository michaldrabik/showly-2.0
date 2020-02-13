package com.michaldrabik.showly2.common.images

import com.michaldrabik.network.Cloud
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageFamily
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.UserTvdbManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class ShowImagesProvider @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userManager: UserTvdbManager,
  private val mappers: Mappers
) {

  suspend fun findCachedImage(show: Show, type: ImageType): Image {
    val cachedImage = database.imagesDao().getByShowId(show.ids.tvdb.id, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type, ImageFamily.SHOW)
      else -> mappers.image.fromDatabase(cachedImage).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(show: Show, type: ImageType, force: Boolean = false): Image {
    val tvdbId = show.ids.tvdb
    val cachedImage = findCachedImage(show, type)
    if (cachedImage.status == Image.Status.AVAILABLE && !force) {
      return cachedImage
    }

    userManager.checkAuthorization()
    val images = cloud.tvdbApi.fetchShowImages(userManager.getToken(), tvdbId.id)

    var typeImages = images.filter { it.keyType == type.key }

    // If requested poster is unavailable try backing up to a fanart
    if (typeImages.isEmpty() && type == ImageType.POSTER) {
      typeImages = images.filter { it.keyType == ImageType.FANART.key }
    }

    // If requested fanart is unavailable try backing up to an episode image
    if (typeImages.isEmpty() && type in arrayOf(ImageType.FANART, ImageType.FANART_WIDE)) {
      val seasons = cloud.traktApi.fetchSeasons(show.ids.trakt.id)
      if (seasons.isNotEmpty()) {
        val episode = seasons[0].episodes.firstOrNull()
        episode?.let { ep ->
          val backupImage = cloud.tvdbApi.fetchEpisodeImage(userManager.getToken(), ep.ids.tvdb)
          backupImage?.let { img -> typeImages = listOf(img) }
        }
      }
    }

    val remoteImage = typeImages.maxBy { it.rating.count }
    val image = when (remoteImage) {
      null -> Image.createUnavailable(type)
      else -> Image(remoteImage.id, tvdbId, type, ImageFamily.SHOW, remoteImage.fileName, remoteImage.thumbnail, Image.Status.AVAILABLE)
    }

    when (image.status) {
      Image.Status.UNAVAILABLE -> database.imagesDao().deleteByShowId(tvdbId.id, image.type.key)
      else -> {
        database.imagesDao().insertShowImage(mappers.image.toDatabase(image))
        storeExtraImage(tvdbId, images, type)
      }
    }

    return image
  }

  private suspend fun storeExtraImage(
    id: IdTvdb,
    images: List<TvdbImage>,
    targetType: ImageType
  ) {
    val extraType = if (targetType == ImageType.POSTER) ImageType.FANART else ImageType.POSTER
    images
      .filter { it.keyType == extraType.key }
      .maxBy { it.rating.count }
      ?.let {
        val extraImage = Image(it.id, id, extraType, ImageFamily.SHOW, it.fileName, it.thumbnail, Image.Status.AVAILABLE)
        database.imagesDao().insertShowImage(mappers.image.toDatabase(extraImage))
      }
  }

  suspend fun loadRemoteImages(show: Show, type: ImageType): List<Image> {
    val tvdbId = show.ids.tvdb

    userManager.checkAuthorization()
    val remoteImages = cloud.tvdbApi.fetchShowImages(userManager.getToken(), tvdbId.id)

    return remoteImages
      .filter { it.keyType == type.key }
      .map {
        Image(it.id, tvdbId, type, ImageFamily.SHOW, it.fileName, it.thumbnail, Image.Status.AVAILABLE)
      }
  }
}
