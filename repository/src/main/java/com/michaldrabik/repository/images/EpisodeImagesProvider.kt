package com.michaldrabik.repository.images

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.RemoteDataSource
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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeImagesProvider @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers
) {

  private suspend fun findCachedImage(episode: Episode, type: ImageType): Image {
    val cachedImage = localSource.showImages.getByEpisodeId(episode.ids.tmdb.id, type.key)
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
    try {
      var remoteImage = remoteSource.tmdb.fetchEpisodeImage(showId.id, episode.season, episode.number)
      if (remoteImage == null && (episode.numberAbs ?: 0) > 0) {
        // Try absolute episode number if present (may happen with certain Anime series)
        remoteImage = remoteSource.tmdb.fetchEpisodeImage(showId.id, episode.season, episode.numberAbs)
      }
      image = when (remoteImage) {
        null -> Image.createUnavailable(FANART)
        else -> Image(
          id = -1,
          idTvdb = tvdbId,
          idTmdb = tmdbId,
          type = FANART,
          family = EPISODE,
          fileUrl = remoteImage.file_path,
          thumbnailUrl = "",
          status = AVAILABLE,
          source = ImageSource.TMDB
        )
      }
    } catch (error: Throwable) {
      Timber.w(error)
    }

    when (image.status) {
      UNAVAILABLE -> localSource.showImages.deleteByEpisodeId(tmdbId.id, image.type.key)
      else -> localSource.showImages.insertEpisodeImage(mappers.image.toDatabaseShow(image))
    }

    return image
  }
}
