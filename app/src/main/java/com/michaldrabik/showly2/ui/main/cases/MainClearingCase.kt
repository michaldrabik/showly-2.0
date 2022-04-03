package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MainClearingCase @Inject constructor(
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
) {

  fun clear() {
    showImagesProvider.clear()
    movieImagesProvider.clear()
    Timber.d("Clearing...")
  }
}
