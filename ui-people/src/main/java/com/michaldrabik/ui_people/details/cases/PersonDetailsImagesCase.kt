package com.michaldrabik.ui_people.details.cases

import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_people.details.recycler.PersonDetailsItem
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class PersonDetailsImagesCase @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider
) {

  suspend fun loadMissingImage(item: PersonDetailsItem.CreditsShowItem, force: Boolean) =
    try {
      val image = showImagesProvider.loadRemoteImage(item.show, item.image.type, force)
      item.copy(isLoading = false, image = image)
    } catch (t: Throwable) {
      Timber.w(t)
      item.copy(isLoading = false, image = Image.createUnavailable(item.image.type))
    }

  suspend fun loadMissingImage(item: PersonDetailsItem.CreditsMovieItem, force: Boolean) =
    try {
      val image = movieImagesProvider.loadRemoteImage(item.movie, item.image.type, force)
      item.copy(isLoading = false, image = image)
    } catch (t: Throwable) {
      Timber.w(t)
      item.copy(isLoading = false, image = Image.createUnavailable(item.image.type))
    }
}
