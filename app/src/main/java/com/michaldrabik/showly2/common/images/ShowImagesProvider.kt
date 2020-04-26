package com.michaldrabik.showly2.common.images

import com.michaldrabik.network.Cloud
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.ImageFamily.SHOW
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.FANART_WIDE
import com.michaldrabik.showly2.model.ImageType.POSTER
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

  private val unavailableCache = mutableListOf<IdTrakt>()

  suspend fun findCachedImage(show: Show, type: ImageType): Image {
    val cachedImage = database.imagesDao().getByShowId(show.ids.tvdb.id, type.key)
    return when (cachedImage) {
      null -> if (unavailableCache.contains(show.ids.trakt)) {
        Image.createUnavailable(type, SHOW)
      } else {
        Image.createUnknown(type, SHOW)
      }
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
    if (typeImages.isEmpty() && type == POSTER) {
      typeImages = images.filter { it.keyType == FANART.key }
    }

    // If requested fanart is unavailable try backing up to an episode image
    if (typeImages.isEmpty() && type in arrayOf(FANART, FANART_WIDE)) {
      val seasons = cloud.traktApi.fetchSeasons(show.ids.trakt.id)
      if (seasons.isNotEmpty()) {
        val episode = seasons[0].episodes?.firstOrNull()
        episode?.let { ep ->
          runCatching {
            val backupImage = cloud.tvdbApi.fetchEpisodeImage(userManager.getToken(), ep.ids?.tvdb ?: -1)
            backupImage?.let { img -> typeImages = listOf(img) }
          }
        }
      }
    }

    val remoteImage = typeImages.maxBy { it.rating?.count ?: 0 }
    val image = when (remoteImage) {
      null -> Image.createUnavailable(type)
      else -> Image(remoteImage.id ?: -1, tvdbId, type, SHOW, remoteImage.fileName ?: "", remoteImage.thumbnail ?: "", Image.Status.AVAILABLE)
    }

    when (image.status) {
      UNAVAILABLE -> {
        unavailableCache.add(show.ids.trakt)
        database.imagesDao().deleteByShowId(tvdbId.id, image.type.key)
      }
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
    val extraType = if (targetType == POSTER) FANART else POSTER
    images
      .filter { it.keyType == extraType.key }
      .maxBy { it.rating?.count ?: 0 }
      ?.let {
        val extraImage = Image(it.id ?: -1, id, extraType, SHOW, it.fileName ?: "", it.thumbnail ?: "", Image.Status.AVAILABLE)
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
        Image(it.id ?: -1, tvdbId, type, SHOW, it.fileName ?: "", it.thumbnail ?: "", Image.Status.AVAILABLE)
      }
  }
}
