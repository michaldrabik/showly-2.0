package com.michaldrabik.repository.images

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.CustomImage
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.tmdb.model.TmdbImage
import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageSource.CUSTOM
import com.michaldrabik.ui_model.ImageSource.TMDB
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieImagesProvider @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository
) {

  private val unavailableCache = mutableSetOf<IdTrakt>()

  suspend fun findCustomImage(traktId: Long, type: ImageType): Image? {
    if (!settingsRepository.isPremium) return null
    val custom = localSource.customImages.getById(traktId, Mode.MOVIES.type, type.key)
    return custom?.let { mappers.image.fromDatabase(it, type) }
  }

  suspend fun findCachedImage(movie: Movie, type: ImageType): Image {
    val custom = findCustomImage(movie.traktId, type)
    if (custom != null) return custom

    val image = localSource.movieImages.getByMovieId(movie.ids.tmdb.id, type.key)
    return when (image) {
      null ->
        if (unavailableCache.contains(movie.ids.trakt)) {
          Image.createUnavailable(type, MOVIE, TMDB)
        } else {
          Image.createUnknown(type, MOVIE, TMDB)
        }
      else -> mappers.image.fromDatabase(image).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(movie: Movie, type: ImageType, force: Boolean = false): Image {
    val tmdbId = movie.ids.tmdb
    val tvdbId = movie.ids.tvdb

    val cachedImage = findCachedImage(movie, type)
    if (cachedImage.status in arrayOf(AVAILABLE, UNAVAILABLE)) {
      if (!force) return cachedImage
      if (force && cachedImage.source == CUSTOM) return cachedImage
    }

    val images = remoteSource.tmdb.fetchMovieImages(tmdbId.id)
    val typeImages = when (type) {
      POSTER -> images.posters ?: emptyList()
      FANART, FANART_WIDE -> images.backdrops ?: emptyList()
      else -> throw Error("Invalid type")
    }

    val remoteImage = findBestImage(typeImages, type)
    val image = when (remoteImage) {
      null -> Image.createUnavailable(type, MOVIE, TMDB)
      else -> Image.createAvailable(movie.ids, type, MOVIE, remoteImage.file_path, TMDB)
    }

    when (image.status) {
      UNAVAILABLE -> {
        unavailableCache.add(movie.ids.trakt)
        localSource.movieImages.deleteByMovieId(tmdbId.id, image.type.key)
      }
      else -> {
        localSource.movieImages.insertMovieImage(mappers.image.toDatabaseMovie(image))
        storeExtraImage(tmdbId, tvdbId, images, type)
      }
    }

    return image
  }

  private suspend fun storeExtraImage(
    tmdbId: IdTmdb,
    tvdbId: IdTvdb,
    images: TmdbImages,
    targetType: ImageType
  ) {
    val extraType = if (targetType == POSTER) FANART else POSTER
    val typeImages = when (extraType) {
      POSTER -> images.posters ?: emptyList()
      FANART, FANART_WIDE -> images.backdrops ?: emptyList()
      else -> throw Error("Invalid type")
    }
    findBestImage(typeImages, extraType)?.let {
      val extraImage = Image(-1, tvdbId, tmdbId, extraType, MOVIE, it.file_path, "", AVAILABLE, TMDB)
      localSource.movieImages.insertMovieImage(mappers.image.toDatabaseMovie(extraImage))
    }
  }

  suspend fun loadRemoteImages(movie: Movie, type: ImageType): List<Image> {
    val tmdbId = movie.ids.tmdb
    val remoteImages = remoteSource.tmdb.fetchMovieImages(tmdbId.id)
    val typeImages = when (type) {
      POSTER -> remoteImages.posters ?: emptyList()
      FANART, FANART_WIDE -> remoteImages.backdrops ?: emptyList()
      else -> throw Error("Invalid type")
    }
    return typeImages.map {
      Image.createAvailable(movie.ids, type, MOVIE, it.file_path, TMDB)
    }
  }

  private fun findBestImage(images: List<TmdbImage>, type: ImageType) =
    images
      .filter { if (type == POSTER) it.isEnglish() else it.isPlain() }
      .sortedWith(compareBy({ it.vote_count }, { it.vote_average }))
      .lastOrNull()
      ?: images.firstOrNull { if (type == POSTER) it.isEnglish() else it.isPlain() }
      ?: images.firstOrNull()

  suspend fun saveCustomImage(traktId: IdTrakt, image: Image, imageFamily: ImageFamily, imageType: ImageType) {
    val imageDb = CustomImage(0, traktId.id, imageFamily.key, imageType.key, image.fullFileUrl)
    localSource.customImages.insertImage(imageDb)
  }

  suspend fun deleteCustomImage(traktId: IdTrakt, imageFamily: ImageFamily, imageType: ImageType) {
    localSource.customImages.deleteById(traktId.id, imageFamily.key, imageType.key)
  }

  suspend fun deleteLocalCache() = localSource.movieImages.deleteAll()

  fun clear() = unavailableCache.clear()
}
