package com.michaldrabik.ui_people.gallery.cases

import com.michaldrabik.ui_base.images.PeopleImagesProvider
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Image
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PersonGalleryImagesCase @Inject constructor(
  private val imagesProvider: PeopleImagesProvider
) {

  suspend fun loadInitialImage(id: IdTmdb) = imagesProvider.loadCachedImage(id)

  suspend fun loadAllImages(id: IdTmdb, initialImage: Image?): List<Image> {
    val initial = listOf(initialImage)
    val images = imagesProvider.loadImages(id).filter { it.fileUrl != initialImage?.fileUrl }
    return (initial + images).filterNotNull()
  }
}
