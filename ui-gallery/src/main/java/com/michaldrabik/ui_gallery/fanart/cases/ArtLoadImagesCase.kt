package com.michaldrabik.ui_gallery.fanart.cases

import com.michaldrabik.common.Config.FANART_GALLERY_IMAGES_LIMIT
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageStatus.AVAILABLE
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ArtLoadImagesCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) {

  suspend fun loadImages(
    id: IdTrakt,
    family: ImageFamily,
    type: ImageType
  ): List<Image> {
    val images = mutableListOf<Image>()
    val initialImage = loadInitialImage(id, family, type)
    if (initialImage.status == AVAILABLE) {
      images.add(initialImage)
    }

    var remoteImages: List<Image> = emptyList()
    if (family == SHOW) {
      val show = showsRepository.detailsShow.load(id)
      remoteImages = showImagesProvider.loadRemoteImages(show, type)
    } else if (family == MOVIE) {
      val movie = moviesRepository.movieDetails.load(id)
      remoteImages = movieImagesProvider.loadRemoteImages(movie, type)
    }
    images.addAll(remoteImages.filter { it.fullFileUrl != initialImage.fullFileUrl })
    return images.take(FANART_GALLERY_IMAGES_LIMIT)
  }

  private suspend fun loadInitialImage(id: IdTrakt, family: ImageFamily, type: ImageType) =
    when (family) {
      SHOW -> {
        val show = showsRepository.detailsShow.load(id)
        showImagesProvider.findCachedImage(show, type)
      }
      MOVIE -> {
        val movie = moviesRepository.movieDetails.load(id)
        movieImagesProvider.findCachedImage(movie, type)
      }
      else -> throw IllegalStateException()
    }

  suspend fun saveCustomImage(id: IdTrakt, image: Image, family: ImageFamily, type: ImageType) {
    when (family) {
      SHOW -> showImagesProvider.saveCustomImage(id, image, family, type)
      MOVIE -> movieImagesProvider.saveCustomImage(id, image, family, type)
      else -> error("Invalid image family")
    }
  }
}
