package com.michaldrabik.repository.images

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.CustomImage
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.aws.model.AwsImages
import com.michaldrabik.data_remote.tmdb.model.TmdbImage
import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageSource.AWS
import com.michaldrabik.ui_model.ImageSource.CUSTOM
import com.michaldrabik.ui_model.ImageSource.TMDB
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.FANART_WIDE
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Show
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowImagesProvider @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
) {

  private val unavailableCache = mutableSetOf<IdTrakt>()
  private var awsImagesCache: AwsImages? = null

  suspend fun findCustomImage(traktId: Long, type: ImageType): Image? {
    if (!settingsRepository.isPremium) return null
    val custom = localSource.customImages.getById(traktId, Mode.SHOWS.type, type.key)
    return custom?.let { mappers.image.fromDatabase(it, type) }
  }

  suspend fun findCachedImage(show: Show, type: ImageType): Image {
    val custom = findCustomImage(show.traktId, type)
    if (custom != null) return custom

    val image = localSource.showImages.getByShowId(show.ids.tmdb.id, type.key)
    return when (image) {
      null ->
        if (unavailableCache.contains(show.ids.trakt)) {
          Image.createUnavailable(type, SHOW)
        } else {
          Image.createUnknown(type, SHOW)
        }
      else -> mappers.image.fromDatabase(image).copy(type = type)
    }
  }

  suspend fun loadRemoteImage(show: Show, type: ImageType, force: Boolean = false): Image {
    val tvdbId = show.ids.tvdb
    val tmdbId = show.ids.tmdb

    val cachedImage = findCachedImage(show, type)
    if (cachedImage.status in arrayOf(AVAILABLE, UNAVAILABLE)) {
      if (!force || cachedImage.source == CUSTOM) {
        return cachedImage
      }
    }

    var source = TMDB
    val images = remoteSource.tmdb.fetchShowImages(tmdbId.id)

    var typeImages = when (type) {
      POSTER -> images.posters ?: emptyList()
      FANART, FANART_WIDE -> images.backdrops ?: emptyList()
      else -> throw Error("Invalid type")
    }
    // If requested poster is unavailable try backing up to a fanart
    if (typeImages.isEmpty() && type == POSTER) {
      typeImages = images.backdrops ?: emptyList()
      if (typeImages.isEmpty()) {
        // Use custom uploaded S3 image as a final backup
        loadAwsImagesCache()
        awsImagesCache?.posters?.find { poster -> poster.idTmdb == tmdbId.id }?.let {
          val path = "posters/${it.idTmdb}.${it.fileType}"
          typeImages = listOf(TmdbImage(path, 0F, 0, "en"))
          source = AWS
        }
      }
    }

    // Use custom uploaded S3 image as a first backup for fanart.
    if (typeImages.isEmpty() && type in arrayOf(FANART, FANART_WIDE)) {
      loadAwsImagesCache()
      val awsImage = awsImagesCache?.fanarts?.find { fanart -> fanart.idTmdb == tmdbId.id }
      if (awsImage != null) {
        val path = "fanarts/${awsImage.idTmdb}.${awsImage.fileType}"
        typeImages = listOf(TmdbImage(path, 0F, 0, "en"))
        source = AWS
      } else {
        // If requested fanart is unavailable try backing up to an episode image
        val seasons = remoteSource.trakt.fetchSeasons(show.traktId)
        if (seasons.isNotEmpty()) {
          val episode = seasons[0].episodes?.firstOrNull()
          episode?.let { ep ->
            runCatching {
              val backupImage = remoteSource.tmdb.fetchEpisodeImage(tmdbId.id, ep.season, ep.number)
              backupImage?.let {
                typeImages = listOf(TmdbImage(it.file_path, 0F, 0, "en"))
              }
            }
          }
        }
      }
    }

    val remoteImage = findBestImage(typeImages, type)
    val image = when (remoteImage) {
      null -> Image.createUnavailable(type)
      else -> Image.createAvailable(show.ids, type, SHOW, remoteImage.file_path, source)
    }

    when (image.status) {
      UNAVAILABLE -> {
        unavailableCache.add(show.ids.trakt)
        localSource.showImages.deleteByShowId(tmdbId.id, image.type.key)
      }
      else -> {
        localSource.showImages.insertShowImage(mappers.image.toDatabaseShow(image))
        saveExtraImage(tmdbId, tvdbId, images, type)
      }
    }

    return image
  }

  private suspend fun saveExtraImage(
    tmdbId: IdTmdb,
    tvdbId: IdTvdb,
    images: TmdbImages,
    targetType: ImageType,
  ) {
    val extraType = if (targetType == POSTER) FANART else POSTER
    val typeImages = when (extraType) {
      POSTER -> images.posters ?: emptyList()
      FANART, FANART_WIDE -> images.backdrops ?: emptyList()
      else -> throw Error("Invalid type")
    }
    findBestImage(typeImages, extraType)?.let {
      val extraImage = Image(-1, tvdbId, tmdbId, extraType, SHOW, it.file_path, "", AVAILABLE, TMDB)
      localSource.showImages.insertShowImage(mappers.image.toDatabaseShow(extraImage))
    }
  }

  suspend fun loadRemoteImages(show: Show, type: ImageType): List<Image> {
    val tmdbId = show.ids.tmdb
    val remoteImages = remoteSource.tmdb.fetchShowImages(tmdbId.id)
    val typeImages = when (type) {
      POSTER -> remoteImages.posters ?: emptyList()
      FANART, FANART_WIDE -> remoteImages.backdrops ?: emptyList()
      else -> throw Error("Invalid type")
    }

    return typeImages
      .map {
        Image.createAvailable(show.ids, type, SHOW, it.file_path, TMDB)
      }
  }

  private suspend fun loadAwsImagesCache() {
    if (awsImagesCache == null) {
      val awsImages = remoteSource.aws.fetchImagesList()
      awsImagesCache = awsImages.copy()
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

  suspend fun deleteLocalCache() = localSource.showImages.deleteAll()

  fun clear() = unavailableCache.clear()
}
