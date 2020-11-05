package com.michaldrabik.ui_base.images

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.tvdb.model.TvdbImage
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.Image.Status.AVAILABLE
import com.michaldrabik.ui_model.Image.Status.UNAVAILABLE
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageSource.TVDB
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.UserTvdbManager
import com.michaldrabik.ui_repository.mappers.Mappers
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
      null ->
        if (unavailableCache.contains(show.ids.trakt)) {
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
    if (cachedImage.status == AVAILABLE && !force) {
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
      else -> Image(
        remoteImage.id ?: -1,
        tvdbId,
        type,
        SHOW,
        remoteImage.fileName ?: "",
        remoteImage.thumbnail ?: "",
        AVAILABLE,
        TVDB
      )
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
        val extraImage = Image(it.id ?: -1, id, extraType, SHOW, it.fileName ?: "", it.thumbnail ?: "", AVAILABLE, TVDB)
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
        Image(it.id ?: -1, tvdbId, type, SHOW, it.fileName ?: "", it.thumbnail ?: "", AVAILABLE, TVDB)
      }
  }

  suspend fun deleteLocalCache() = database.imagesDao().deleteAll()
}
