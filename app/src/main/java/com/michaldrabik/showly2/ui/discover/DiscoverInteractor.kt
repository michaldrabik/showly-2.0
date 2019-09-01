package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Image.Status.AVAILABLE
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TrendingShow
import com.michaldrabik.storage.repository.UserRepository
import java.lang.System.currentTimeMillis
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val userRepository: UserRepository,
  private val mappers: Mappers
) {

  suspend fun loadTrendingShows(): List<Show> {
    val stamp = database.trendingShowsDao().getMostRecent()?.createdAt ?: 0
    if (currentTimeMillis() - stamp < Config.TRENDING_SHOWS_CACHE_DURATION) {
      return database.trendingShowsDao().getAll().map { mappers.show.fromDatabase(it) }
    }

    val remoteShows = cloud.traktApi.fetchTrendingShows().map { mappers.show.fromNetwork(it) }
    database.showsDao().upsert(remoteShows.map { mappers.show.toDatabase(it) })
    val timestamp = currentTimeMillis()
    database.trendingShowsDao().deleteAllAndInsert(remoteShows.map {
      TrendingShow(idTrakt = it.ids.trakt, createdAt = timestamp, updatedAt = timestamp)
    })

    return remoteShows
  }

  suspend fun findCachedImage(show: Show, type: ImageType): Image {
    val cachedImage = database.imagesDao().getById(show.ids.tvdb, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type)
      else -> mappers.image.fromDb(cachedImage).copy(type = type)
    }
  }

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean): Image {
    val tvdbId = show.ids.tvdb
    val cachedImage = findCachedImage(show, type)
    if (cachedImage.status == AVAILABLE && !force) {
      return cachedImage
    }

    checkAuthorization()
    val images = cloud.tvdbApi.fetchImages(userRepository.tvdbToken, tvdbId, type.key)
    val remoteImage = images.firstOrNull()

    val image = when (remoteImage) {
      null -> Image.createUnavailable(type)
      else -> Image(tvdbId, type, remoteImage.fileName, remoteImage.thumbnail, AVAILABLE)
    }

    when (image.status) {
      UNAVAILABLE -> database.imagesDao().deleteById(tvdbId)
      else -> database.imagesDao().insert(mappers.image.toDb(image))
    }

    return image
  }

  private suspend fun checkAuthorization() {
    if (!userRepository.isTvdbAuthorized) {
      val token = cloud.tvdbApi.authorize()
      userRepository.tvdbToken = token.token
    }
  }

  private fun onError(error: Throwable) {
    //TODO
  }
}