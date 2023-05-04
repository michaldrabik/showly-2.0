package com.michaldrabik.ui_movie.sections.collections.details.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.sections.collections.details.recycler.MovieDetailsCollectionItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsCollectionTranslationsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository,
) {

  suspend fun loadMissingTranslation(
    item: MovieDetailsCollectionItem.MovieItem,
    language: String
  ) = withContext(dispatchers.IO) {
    try {
      val translation = translationsRepository.loadTranslation(item.movie, language) ?: Translation.EMPTY
      return@withContext item.copy(translation = translation)
    } catch (error: Throwable) {
      Timber.w(error)
      return@withContext item.copy(translation = Translation.EMPTY)
    }
  }
}
