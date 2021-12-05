package com.michaldrabik.ui_people.gallery.cases

import com.michaldrabik.repository.PeopleRepository
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily
import com.michaldrabik.ui_model.ImageSource
import com.michaldrabik.ui_model.ImageType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class PersonGalleryImagesCase @Inject constructor(
  private val peopleRepository: PeopleRepository
) {

  suspend fun loadInitialImage(id: IdTmdb): Image? {
    val imagePath = peopleRepository.loadDefaultImage(id)
    imagePath?.let {
      return Image.createAvailable(Ids.EMPTY, ImageType.FANART, ImageFamily.SHOW, it, ImageSource.TMDB)
    }
    return null
  }
}
