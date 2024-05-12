package com.michaldrabik.repository.images

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.tmdb.model.TmdbImage
import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieImagesProvider @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private var translationsRepository: TranslationsRepository
) {

  private val unavailableCache = mutableSetOf<IdTrakt>()

  suspend fun findCachedImage(movie: Movie, type: ImageType): Image =
    withContext(dispatchers.IO) {
      val image = localSource.movieImages.getByMovieId(movie.ids.tmdb.id, type.key)
      when (image) {
        null ->
          if (unavailableCache.contains(movie.ids.trakt)) {
            Image.createUnavailable(type, MOVIE, TMDB)
          } else {
            Image.createUnknown(type, MOVIE, TMDB)
          }
        else -> mappers.image.fromDatabase(image).copy(type = type)
      }
    }

  suspend fun loadRemoteImage(movie: Movie, type: ImageType, force: Boolean = false): Image =
    withContext(dispatchers.IO) {
      val tmdbId = movie.ids.tmdb
      val tvdbId = movie.ids.tvdb

      val cachedImage = findCachedImage(movie, type)
      if (cachedImage.status in arrayOf(AVAILABLE, UNAVAILABLE)) {
        if (!force) return@withContext cachedImage
        if (force && cachedImage.source == CUSTOM) return@withContext cachedImage
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

      image
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

  suspend fun loadRemoteImages(movie: Movie, type: ImageType): List<Image> =
    withContext(dispatchers.IO) {
      val tmdbId = movie.ids.tmdb
      val remoteImages = remoteSource.tmdb.fetchMovieImages(tmdbId.id)
      val typeImages = when (type) {
        POSTER -> remoteImages.posters ?: emptyList()
        FANART, FANART_WIDE -> remoteImages.backdrops ?: emptyList()
        else -> throw Error("Invalid type")
      }
      typeImages.map {
        Image.createAvailable(movie.ids, type, MOVIE, it.file_path, TMDB)
      }
    }

  private fun findBestImage(images: List<TmdbImage>, type: ImageType): TmdbImage? {
    val language = translationsRepository.getLanguage()
    val comparator = when (type) {
      POSTER -> compareBy<TmdbImage> { it.isLanguage(language) }
        .thenBy { it.isEnglish() }
        .thenBy { it.isPlain() }
      else -> compareBy<TmdbImage> { it.isPlain() }
        .thenBy { it.isLanguage(language) }
        .thenBy { it.isEnglish() }
    }
    return images.maxWithOrNull(comparator.thenBy { it.getVoteScore() })
  }

  suspend fun deleteLocalCache() = withContext(dispatchers.IO) {
    localSource.movieImages.deleteAll()
  }

  fun clear() = unavailableCache.clear()
}
