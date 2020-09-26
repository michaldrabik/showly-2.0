package com.michaldrabik.showly2.ui.show.gallery

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.Config.FANART_GALLERY_IMAGES_LIMIT
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class FanartLoadImagesCase @Inject constructor(
    private val showsRepository: ShowsRepository,
    private val imagesProvider: ShowImagesProvider
) {

  suspend fun loadInitialImage(id: IdTrakt): Image {
    val show = showsRepository.detailsShow.load(id)
    return imagesProvider.findCachedImage(show, ImageType.FANART)
  }

  suspend fun loadAllImages(id: IdTrakt, initialImage: Image): List<Image> {
    val images = mutableListOf<Image>()
    if (initialImage.status == Image.Status.AVAILABLE) {
      images.add(initialImage)
    }

    val show = showsRepository.detailsShow.load(id)
    val allImages = imagesProvider.loadRemoteImages(show, ImageType.FANART)
      .filter { it.fileUrl != initialImage.fileUrl }
      .take(FANART_GALLERY_IMAGES_LIMIT)
    images.addAll(allImages)
    return images
  }
}
