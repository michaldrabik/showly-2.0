package com.michaldrabik.repository.images

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.PersonImage
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageSource
import com.michaldrabik.ui_model.ImageType
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleImagesProvider @Inject constructor(
  private val localSource: LocalDataSource,
  private val remoteSource: RemoteDataSource,
) {

  suspend fun loadCachedImage(personTmdbId: IdTmdb): Image? {
    val localPerson = localSource.people.getById(personTmdbId.id)
    return localPerson?.image?.let {
      return Image.createAvailable(
        ids = Ids.EMPTY,
        type = ImageType.PROFILE,
        family = ImageFamily.PROFILE,
        path = it,
        source = ImageSource.TMDB
      )
    }
  }

  suspend fun loadImages(personTmdbId: IdTmdb): List<Image> {
    val localTimestamp = localSource.peopleImages.getTimestampForPerson(personTmdbId.id) ?: 0
    if (localTimestamp + Config.PEOPLE_IMAGES_CACHE_DURATION > nowUtcMillis()) {
      Timber.d("Returning cached result. Cache still valid for ${(localTimestamp + Config.PEOPLE_IMAGES_CACHE_DURATION) - nowUtcMillis()} ms")
      val local = localSource.peopleImages.getAll(personTmdbId.id)
      return local.map {
        Image.createAvailable(
          ids = Ids.EMPTY,
          type = ImageType.PROFILE,
          family = ImageFamily.PROFILE,
          path = it.filePath,
          source = ImageSource.TMDB
        )
      }
    }

    val images = (remoteSource.tmdb.fetchPersonImages(personTmdbId.id).profiles ?: emptyList())
      .filter { it.file_path.isNotBlank() }
    val dbImages = images.map {
      PersonImage(
        id = 0,
        idTmdb = personTmdbId.id,
        filePath = it.file_path,
        createdAt = nowUtc(),
        updatedAt = nowUtc()
      )
    }

    localSource.peopleImages.insert(personTmdbId.id, dbImages)

    return images.map {
      Image.createAvailable(
        ids = Ids.EMPTY,
        type = ImageType.PROFILE,
        family = ImageFamily.PROFILE,
        path = it.file_path,
        source = ImageSource.TMDB
      )
    }
  }
}
