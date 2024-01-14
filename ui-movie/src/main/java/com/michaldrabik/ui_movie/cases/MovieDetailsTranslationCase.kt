package com.michaldrabik.ui_movie.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsTranslationCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val translationsRepository: TranslationsRepository
) {

  suspend fun loadTranslation(movie: Movie): Translation? = withContext(dispatchers.IO) {
    val language = translationsRepository.getLanguage()
    if (language == DEFAULT_LANGUAGE) {
      return@withContext null
    }
    translationsRepository.loadTranslation(movie, language)
  }
}
