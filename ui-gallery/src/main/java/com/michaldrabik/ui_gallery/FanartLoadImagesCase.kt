package com.michaldrabik.ui_gallery

import com.michaldrabik.common.Config.FANART_GALLERY_IMAGES_LIMIT
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_repository.movies.MoviesRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class FanartLoadImagesCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

  suspend fun loadInitialImage(id: IdTrakt, type: ImageFamily): Image {
    if (type == SHOW) {
      val show = showsRepository.detailsShow.load(id)
      return showImagesProvider.findCachedImage(show, FANART)
    } else if (type == MOVIE) {
      val movie = moviesRepository.movieDetails.load(id)
      return movieImagesProvider.findCachedImage(movie, FANART)
    }
    throw IllegalStateException()
  }

  suspend fun loadAllImages(id: IdTrakt, type: ImageFamily, initialImage: Image): List<Image> {
    val images = mutableListOf<Image>()
    if (initialImage.status == ImageStatus.AVAILABLE) {
      images.add(initialImage)
    }

    var remoteImages: List<Image> = emptyList()
    if (type == SHOW) {
      val show = showsRepository.detailsShow.load(id)
      remoteImages = showImagesProvider.loadRemoteImages(show, FANART)
    } else if (type == MOVIE) {
      val movie = moviesRepository.movieDetails.load(id)
      remoteImages = movieImagesProvider.loadRemoteImages(movie, FANART)
    }
    images.addAll(remoteImages.filter { it.fileUrl != initialImage.fileUrl })
    return images.take(FANART_GALLERY_IMAGES_LIMIT)
  }
}
