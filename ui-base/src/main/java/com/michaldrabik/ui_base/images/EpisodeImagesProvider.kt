package com.michaldrabik.ui_base.images

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.EPISODE
import com.michaldrabik.ui_model.ImageSource
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeImagesProvider @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  private suspend fun findCachedImage(episode: Episode, type: ImageType): Image {
    val cachedImage = database.showImagesDao().getByEpisodeId(episode.ids.tmdb.id, type.key)
    return when (cachedImage) {
      null -> Image.createUnknown(type, EPISODE)
      else -> mappers.image.fromDatabase(cachedImage).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(showId: IdTmdb, episode: Episode): Image {
    val tvdbId = episode.ids.tvdb
    val tmdbId = episode.ids.tmdb
    val cachedImage = findCachedImage(episode, FANART)
    if (cachedImage.status == AVAILABLE) {
      return cachedImage
    }

    var image = Image.createUnavailable(FANART)
    runCatching {
      val remoteImage = cloud.tmdbApi.fetchEpisodeImage(showId.id, episode.season, episode.number)
      image = when (remoteImage) {
        null -> Image.createUnavailable(FANART)
        else -> Image(
          -1,
          tvdbId,
          tmdbId,
          FANART,
          EPISODE,
          remoteImage.file_path,
          "",
          AVAILABLE,
          ImageSource.TMDB
        )
      }
    }

    when (image.status) {
      UNAVAILABLE -> database.showImagesDao().deleteByEpisodeId(tmdbId.id, image.type.key)
      else -> database.showImagesDao().insertEpisodeImage(mappers.image.toDatabaseShow(image))
    }

    return image
  }
}
